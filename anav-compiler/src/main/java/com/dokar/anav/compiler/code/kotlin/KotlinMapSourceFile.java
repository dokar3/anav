package com.dokar.anav.compiler.code.kotlin;

import com.dokar.anav.compiler.NavMap;
import com.dokar.anav.compiler.NavMap.Dest;
import com.dokar.anav.compiler.Utils;
import com.dokar.anav.compiler.code.SourceFile;
import com.squareup.kotlinpoet.FileSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.PropertySpec;
import com.squareup.kotlinpoet.TypeNames;
import com.squareup.kotlinpoet.TypeSpec;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import javax.annotation.processing.Filer;

public class KotlinMapSourceFile implements SourceFile {

    private final FileSpec ktFile;

    public KotlinMapSourceFile(@NotNull NavMap navMap) {
        String packageName = navMap.packageName;
        if (packageName == null) {
            throw new IllegalStateException(
                    "Package name of NavNap must not be null");
        }

        TypeSpec.Builder typeSpecBuilder = createTypeSpec(navMap);

        typeSpecBuilder = KotlinPoetHelper.addSuppressAnnotation(
                typeSpecBuilder, KotlinPoetHelper.SUPPRESS_REDUNDANT_MODIFIER);

        this.ktFile = FileSpec.builder(packageName, navMap.name)
                .addType(typeSpecBuilder.build())
                .addComment(Utils.GEN_CODE_COMMENT)
                .build();
    }

    private TypeSpec.Builder createTypeSpec(NavMap navMap) {
        String className = Utils.capitalize(navMap.name);

        TypeSpec.Builder typeSpecBuilder = TypeSpec.objectBuilder(className);

        // add fields
        for (Dest dest : navMap.destinations.values()) {
            typeSpecBuilder.addProperty(getPropertySpec(dest));
        }

        // add sub classes
        for (NavMap subGroup : navMap.subGroups.values()) {
            typeSpecBuilder.addType(createTypeSpec(subGroup).build());
        }

        return typeSpecBuilder;
    }

    private PropertySpec getPropertySpec(Dest dest) {
        String fieldName = dest.name;
        return PropertySpec.builder(fieldName, TypeNames.STRING)
                .addModifiers(KModifier.CONST)
                .initializer("%S", dest.className)
                .build();
    }

    public FileSpec getKtFile() {
        return ktFile;
    }

    @Override
    public String toString() {
        return getKtFile().toString();
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
