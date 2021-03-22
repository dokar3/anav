package com.dokar.anav.compiler.code.java;

import com.dokar.anav.compiler.NavArgs;
import com.dokar.anav.compiler.NavArgs.Arg;
import com.dokar.anav.compiler.Utils;
import com.dokar.anav.compiler.code.SourceFile;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

public class JavaArgsSourceFile implements SourceFile {

    private final JavaFile javaFile;

    public JavaArgsSourceFile(@NotNull NavArgs navArgs) {
        String packageName = navArgs.sourcePackageName;
        if (packageName == null) {
            throw new IllegalStateException(
                    "package name of NavArgs must not be null");
        }

        String classSimpleName = navArgs.sourceClassName;
        if (classSimpleName == null) {
            throw new IllegalStateException(
                    "class name of NavArgs must not be null");
        }

        TypeSpec typeSpec = createTypeSpec(navArgs, classSimpleName);
        this.javaFile = JavaFile.builder(packageName, typeSpec)
                .addFileComment(Utils.GEN_CODE_COMMENT)
                .build();
    }

    private TypeSpec createTypeSpec(NavArgs navArgs, String classSimpleName) {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(classSimpleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        Map<String, TypeSpec.Builder> innerClassBuilders = new HashMap<>();

        for (Map.Entry<String, Set<Arg>> entry : navArgs.args.entrySet()) {
            String className = entry.getKey();

            Set<Arg> classArgs = entry.getValue();

            TypeSpec.Builder targetBuilder;
            if (className == null || className.length() == 0) {
                targetBuilder = typeSpecBuilder;
            } else if (innerClassBuilders.containsKey(className)) {
                targetBuilder = innerClassBuilders.get(className);
            } else {
                targetBuilder = createInnerClassBuilder(className);
                innerClassBuilders.put(className, targetBuilder);
            }

            List<FieldSpec> extraFields = new ArrayList<>();

            for (Arg arg : classArgs) {
                FieldSpec extraField = createExtraField(arg);
                MethodSpec setterSpec = createArgSetter(arg, extraField);
                MethodSpec getterSpec = createArgGetter(arg, extraField);

                // add field and methods
                extraFields.add(extraField);
                targetBuilder.addMethod(setterSpec);
                targetBuilder.addMethod(getterSpec);
            }

            targetBuilder.addFields(extraFields);
        }

        for (Map.Entry<String, TypeSpec.Builder> builder
                : innerClassBuilders.entrySet()) {
            typeSpecBuilder.addType(builder.getValue().build());
        }

        return typeSpecBuilder.build();
    }

    private TypeSpec.Builder createInnerClassBuilder(String name) {
        return TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    }

    private FieldSpec createExtraField(Arg arg) {
        String fieldName = Utils.variableUpperCase(arg.name);
        String[] names = Utils.getPackageAndClassSimpleName(arg.className);
        String value = names[0] + ".extra." + fieldName;
        return FieldSpec.builder(String.class, fieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", value)
                .build();
    }

    private MethodSpec createArgSetter(Arg arg, FieldSpec extraField) {
        String funcName = "set" + Utils.capitalize(arg.name);

        CodeBlock codeBlock = CodeBlock.builder()
                .add("intent.putExtra($N, $N)", extraField.name, arg.name)
                .build();

        TypeName argTypeName = JavaPoetHelper.typeNameOf(arg.typeClassName);

        TypeName intentType = JavaPoetHelper.typeNameOf(Utils.TYPE_INTENT);

        return MethodSpec.methodBuilder(funcName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(intentType, "intent")
                .addParameter(argTypeName, arg.name)
                .addStatement(codeBlock)
                .returns(TypeName.VOID)
                .build();
    }

    private MethodSpec createArgGetter(Arg arg, FieldSpec extraField) {
        String funcName = "get" + Utils.capitalize(arg.name);

        TypeName returnType = JavaPoetHelper.typeNameOf(arg.typeClassName);

        String returnBlock = "return " + intentGetExtraBlock(arg.typeClassName);
        CodeBlock codeBlock = CodeBlock.builder()
                .add(returnBlock, extraField.name)
                .build();

        TypeName intentType = JavaPoetHelper.typeNameOf(Utils.TYPE_INTENT);

        return MethodSpec.methodBuilder(funcName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(intentType, "intent")
                .addStatement(codeBlock)
                .returns(returnType)
                .build();
    }

    private String intentGetExtraBlock(String classCanonicalName) {
        Map<String, String> getters = Utils.INTENT_EXTRA_GETTERS;
        if (getters.containsKey(classCanonicalName)) {
            return "intent." + getters.get(classCanonicalName);
        } else {
            throw new IllegalStateException("Unsupported argument type: " +
                    classCanonicalName);
        }
    }

    @Override
    public void writeTo(File file) throws IOException {
        javaFile.writeTo(file);
    }

    @Override
    public void writeTo(Filer filer) throws IOException {
        javaFile.writeTo(filer);
    }
}
