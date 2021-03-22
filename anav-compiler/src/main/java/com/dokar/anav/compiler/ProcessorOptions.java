package com.dokar.anav.compiler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessorOptions {

    private static final String OPT_DEBUG = "anav.debug";
    private static final String OPT_BUILD_DIR = "anav.buildDir";
    private static final String OPT_SOURCE_DIR = "anav.sourceDir";
    private static final String OPT_PACKAGE_NAME = "anav.packageName";
    private static final String OPT_NAV_MAP_ClASS_NAME = "anav.navMapClassName";
    private static final String OPT_NAV_ARGS_CLASS_NAME = "anav.navArgsClassName";
    private static final String OPT_REMOVE_ACTIVITY_SUFFIX = "anav.removeActivitySuffix";

    String sourcePackageName;
    String buildDir;
    String sourceDir;
    String navMapClassName;
    String navArgsClassName;
    boolean debug;
    boolean removeActivitySuffix;

    public ProcessorOptions(
            String sourcePackageName,
            String buildDir,
            String sourceDir,
            String navMapClassName,
            String navArgsClassName,
            boolean removeActivitySuffix,
            boolean debug) {
        this.sourcePackageName = sourcePackageName;
        this.buildDir = buildDir;
        this.sourceDir = sourceDir;
        this.navMapClassName = navMapClassName;
        this.navArgsClassName = navArgsClassName;
        this.removeActivitySuffix = removeActivitySuffix;
        this.debug = debug;
    }

    public static ProcessorOptions from(Map<String, String> optionsMap) {
        boolean debug = "true".equals(optionsMap.get(OPT_DEBUG));
        boolean removeActivitySuffix =
                "true".equals(optionsMap.get(OPT_REMOVE_ACTIVITY_SUFFIX));
        return new ProcessorOptions(
                optionsMap.get(OPT_PACKAGE_NAME),
                optionsMap.get(OPT_BUILD_DIR),
                optionsMap.get(OPT_SOURCE_DIR),
                optionsMap.get(OPT_NAV_MAP_ClASS_NAME),
                optionsMap.get(OPT_NAV_ARGS_CLASS_NAME),
                removeActivitySuffix,
                debug);
    }

    public static Set<String> optionKeys() {
        Set<String> set = new HashSet<>();
        set.add(OPT_DEBUG);
        set.add(OPT_PACKAGE_NAME);
        set.add(OPT_BUILD_DIR);
        set.add(OPT_SOURCE_DIR);
        set.add(OPT_NAV_MAP_ClASS_NAME);
        set.add(OPT_NAV_ARGS_CLASS_NAME);
        set.add(OPT_REMOVE_ACTIVITY_SUFFIX);
        return set;
    }
}
