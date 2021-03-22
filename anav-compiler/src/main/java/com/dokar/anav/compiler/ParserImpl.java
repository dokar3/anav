package com.dokar.anav.compiler;

import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

public class ParserImpl implements Parser {

    private final Gson gson;

    public ParserImpl() {
        this.gson = new Gson();
    }

    @Nullable
    @Override
    public NavMap parseMap(String json) {
        if (json == null || json.length() == 0) {
            return null;
        }

        NavMap navMap = null;

        try {
            navMap = gson.fromJson(json, NavMap.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return navMap;
    }

    @Nullable
    @Override
    public String mapToJson(NavMap map) {
        if (map == null) {
            return null;
        }

        String json = null;

        try {
            json = gson.toJson(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    @Nullable
    @Override
    public NavArgs parseArgs(String json) {
        if (json == null || json.length() == 0) {
            return null;
        }

        NavArgs navArgs = null;

        try {
            navArgs = gson.fromJson(json, NavArgs.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return navArgs;
    }

    @Nullable
    @Override
    public String argsToJson(NavArgs args) {
        if (args == null) {
            return null;
        }

        String json = null;

        try {
            json = gson.toJson(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }
}
