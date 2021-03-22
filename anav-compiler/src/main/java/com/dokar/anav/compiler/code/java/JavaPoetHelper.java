package com.dokar.anav.compiler.code.java;

import com.dokar.anav.compiler.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import org.jetbrains.annotations.NotNull;

public class JavaPoetHelper {

    @NotNull
    public static TypeName typeNameOf(@NotNull String classCanonicalName) {
        String[] names = Utils.getPackageAndClassSimpleName(classCanonicalName);
        return ClassName.get(names[0], names[1]);
    }
}
