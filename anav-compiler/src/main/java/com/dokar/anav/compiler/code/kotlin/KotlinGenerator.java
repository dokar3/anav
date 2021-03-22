package com.dokar.anav.compiler.code.kotlin;

import com.dokar.anav.compiler.NavArgs;
import com.dokar.anav.compiler.NavMap;
import com.dokar.anav.compiler.code.SourceFile;
import com.dokar.anav.compiler.code.SourceFileGenerator;

import org.jetbrains.annotations.NotNull;

public class KotlinGenerator implements SourceFileGenerator {

    @NotNull
    @Override
    public SourceFile genMapSourceFile(@NotNull NavMap navMap) {
        return new KotlinMapSourceFile(navMap);
    }

    @NotNull
    @Override
    public SourceFile genArgsSourceFile(@NotNull NavArgs navArgs) {
        return new KotlinArgsSourceFile(navArgs);
    }
}
