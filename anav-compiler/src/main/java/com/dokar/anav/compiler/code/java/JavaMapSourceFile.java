package com.dokar.anav.compiler.code.java;

import com.dokar.anav.compiler.NavMap;
import com.dokar.anav.compiler.NavMap.Dest;
import com.dokar.anav.compiler.Utils;
import com.dokar.anav.compiler.code.SourceFile;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

public class JavaMapSourceFile implements SourceFile {
    private final JavaFile javaFile;

    public JavaMapSourceFile(@NotNull NavMap navMap) {
        String packageName = navMap.packageName;
        if (packageName == null) {
            throw new IllegalStateException(
                    "Package name of NavMap must not be null");
        }
        this.javaFile = JavaFile.builder(packageName, getTypeSpec(navMap))
                .addFileComment(Utils.GEN_CODE_COMMENT)
                .build();
    }

    private TypeSpec getTypeSpec(@NotNull NavMap navMap) {
        String className = Utils.capitalize(navMap.name);

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC);
        if (!navMap.isRoot) {
            typeSpecBuilder.addModifiers(Modifier.STATIC);
        }
        typeSpecBuilder.addModifiers(Modifier.FINAL);

        // add fields
        for (Dest dest : navMap.destinations.values()) {
            typeSpecBuilder.addField(getFieldSpec(dest));
        }

        // add sub classes
        for (NavMap subGroup : navMap.subGroups.values()) {
            typeSpecBuilder.addType(getTypeSpec(subGroup));
        }

        return typeSpecBuilder.build();
    }

    private FieldSpec getFieldSpec(Dest dest) {
        String fieldName = dest.name;
        return FieldSpec.builder(String.class, fieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", dest.className)
                .build();
    }

    public JavaFile getJavaFile() {
        return javaFile;
    }

    @Override
    public String toString() {
        return getJavaFile().toString();
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
