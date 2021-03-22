package com.dokar.anav.compiler.code.java;

import com.dokar.anav.compiler.NavArgs;
import com.dokar.anav.compiler.NavMap;
import com.dokar.anav.compiler.code.SourceFile;
import com.dokar.anav.compiler.code.SourceFileGenerator;

import org.jetbrains.annotations.NotNull;

public class JavaGenerator implements SourceFileGenerator {

    @NotNull
    @Override
    public SourceFile genMapSourceFile(@NotNull NavMap navMap) {
        return new JavaMapSourceFile(navMap);
    }

    @NotNull
    @Override
    public SourceFile genArgsSourceFile(@NotNull NavArgs navArgs) {
        return new JavaArgsSourceFile(navArgs);
    }
}
