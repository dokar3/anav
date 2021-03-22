package com.dokar.anav.compiler;

import org.jetbrains.annotations.Nullable;

public interface Parser {
    @Nullable
    NavMap parseMap(String json);

    @Nullable
    String mapToJson(NavMap group);

    @Nullable
    NavArgs parseArgs(String json);

    @Nullable
    String argsToJson(NavArgs args);

}
