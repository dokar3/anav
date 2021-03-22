package com.dokar.anav.compiler.code;

import com.dokar.anav.compiler.NavArgs;
import com.dokar.anav.compiler.NavMap;

import org.jetbrains.annotations.NotNull;

public interface SourceFileGenerator {

    @NotNull
    SourceFile genMapSourceFile(@NotNull NavMap navMap);

    @NotNull
    SourceFile genArgsSourceFile(@NotNull NavArgs navArgs);
}
