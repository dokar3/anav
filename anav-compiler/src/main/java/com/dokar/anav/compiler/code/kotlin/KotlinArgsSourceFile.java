package com.dokar.anav.compiler.code.kotlin;

import com.dokar.anav.compiler.NavArgs;
import com.dokar.anav.compiler.NavArgs.Arg;
import com.dokar.anav.compiler.Utils;
import com.dokar.anav.compiler.code.SourceFile;
import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.FileSpec;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.PropertySpec;
import com.squareup.kotlinpoet.TypeName;
import com.squareup.kotlinpoet.TypeNames;
import com.squareup.kotlinpoet.TypeSpec;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;

public class KotlinArgsSourceFile implements SourceFile {

    private final FileSpec ktFile;

    public KotlinArgsSourceFile(@NotNull NavArgs navArgs) {
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

        TypeSpec.Builder typeSpecBuilder = createTypeSpec(navArgs, classSimpleName);

        typeSpecBuilder = KotlinPoetHelper.addSuppressAnnotation(
                typeSpecBuilder, KotlinPoetHelper.SUPPRESS_REDUNDANT_MODIFIER);

        this.ktFile = FileSpec.builder(packageName, classSimpleName)
                .addType(typeSpecBuilder.build())
                .addComment(Utils.GEN_CODE_COMMENT)
                .build();
    }

    private TypeSpec.Builder createTypeSpec(NavArgs navArgs, String classSimpleName) {
        TypeSpec.Builder typeSpecBuilder = TypeSpec.objectBuilder(classSimpleName);

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

            List<PropertySpec> argFields = new ArrayList<>();

            for (Arg arg : classArgs) {
                PropertySpec extraField = createExtraField(arg);
                PropertySpec argField = createArgField(arg, extraField);

                // add fields and methods
                targetBuilder.addProperty(extraField);
                argFields.add(argField);
            }

            targetBuilder.addProperties(argFields);
        }

        for (Map.Entry<String, TypeSpec.Builder> builder
                : innerClassBuilders.entrySet()) {
            typeSpecBuilder.addType(builder.getValue().build());
        }

        return typeSpecBuilder;
    }

    private TypeSpec.Builder createInnerClassBuilder(String name) {
        return TypeSpec.objectBuilder(name);
    }

    private PropertySpec createExtraField(Arg arg) {
        String fieldName = Utils.variableUpperCase(arg.name);
        String[] names = Utils.getPackageAndClassSimpleName(arg.className);
        String value = names[0] + ".extra." + fieldName;
        return PropertySpec.builder(fieldName, TypeNames.STRING)
                .addModifiers(KModifier.CONST)
                .initializer("%S", value)
                .build();
    }

    private PropertySpec createArgField(Arg arg, PropertySpec extraField) {
        String returnStatement = intentGetExtraBlock(arg.typeClassName);
        FunSpec getter = FunSpec.getterBuilder()
                .addStatement(returnStatement, extraField.getName())
                .build();

        TypeName ktType = javaTypeToKtType(arg.typeClassName);

        String setStatement = "putExtra(%N, value)";
        FunSpec setter = FunSpec.setterBuilder()
                .addParameter("value", ktType)
                .addStatement(setStatement, extraField.getName())
                .build();

        Class<?> intent;
        try {
            intent = Class.forName(Utils.TYPE_INTENT);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cannot get Intent class.");
        }

        TypeName nullableKtType = ktType.copy(true, ktType.getAnnotations());

        return PropertySpec.builder(arg.name, nullableKtType)
                .mutable(true)
                .receiver(intent)
                .getter(getter)
                .setter(setter)
                .build();
    }

    private TypeName javaTypeToKtType(String classCanonicalName) {
        TypeName ktType = KotlinPoetHelper.JAVA_TO_KT_TYPE_MAP
                .get(classCanonicalName);

        if (ktType != null) {
            return ktType;
        }

        String[] names = Utils.getPackageAndClassSimpleName(classCanonicalName);
        return new ClassName(names[0], names[1]);
    }

    private String intentGetExtraBlock(String classCanonicalName) {
        Map<String, String> getters = Utils.INTENT_EXTRA_GETTERS;
        if (getters.containsKey(classCanonicalName)) {
            return "return " + getters.get(classCanonicalName)
                    .replace('$', '%');
        } else {
            throw new IllegalStateException("Unsupported argument type");
        }
    }

    @Override
    public void writeTo(File file) throws IOException {
        ktFile.writeTo(file);
    }

    @Override
    public void writeTo(Filer filer) throws IOException {
        ktFile.writeTo(filer);
    }
}
