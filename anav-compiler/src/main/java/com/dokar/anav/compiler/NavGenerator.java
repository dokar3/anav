package com.dokar.anav.compiler;

import com.dokar.anav.compiler.code.SourceFile;
import com.dokar.anav.compiler.code.SourceFileGenerator;
import com.dokar.anav.compiler.code.java.JavaGenerator;
import com.dokar.anav.compiler.code.kotlin.KotlinGenerator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class NavGenerator {

    private static final String DEF_CLASS_PACKAGE = "anav";

    private static final String DEF_NAV_MAP_CLASS_NAME = "NavMap";

    private static final String DEF_NAV_ARGS_CLASS_NAME = "NavArgs";

    private final NavMap navMap;

    private final NavArgs navArgs;

    private final Parser parser;

    private final SourceFileGenerator sourceGenerator;

    private NavGenerator(@NotNull NavMap navMap,
                         @NotNull NavArgs navArgs,
                         @NotNull Parser parser,
                         @NotNull SourceFileGenerator generator) {
        this.navMap = navMap;
        this.navArgs = navArgs;
        this.parser = parser;
        this.sourceGenerator = generator;
    }

    public boolean isEmptyMap() {
        return navMap.destinations.isEmpty() && navMap.subGroups.isEmpty();
    }

    public boolean isEmptyArgs() {
        return navArgs.isEmpty();
    }

    @NotNull
    public SourceFile genMapSourceFile() {
        return sourceGenerator.genMapSourceFile(navMap);
    }

    @Nullable
    public String getNavMapJson() {
        return parser.mapToJson(navMap);
    }

    @NotNull
    public SourceFile genArgsSourceFile() {
        return sourceGenerator.genArgsSourceFile(navArgs);
    }

    @Nullable
    public String getNavArgsJson() {
        return parser.argsToJson(navArgs);
    }

    public void addDestination(
            @NotNull String name,
            @NotNull String packageName,
            @NotNull String classSimpleName,
            @Nullable String groupName) {
        navMap.addDestination(name, packageName, classSimpleName, groupName);
    }

    /**
     * Add an argument
     */
    public void addArgument(
            @NotNull String packageName,
            @NotNull String classSimpleName,
            @NotNull String argName,
            @NotNull String argGroup,
            @Nullable String argType) {
        navArgs.addArg(packageName, classSimpleName, argName, argGroup, argType);
    }

    public void cleanDestinations(
            @NotNull String packageName,
            @NotNull String classSimpleName) {
        navMap.cleanDestination(packageName, classSimpleName);
    }

    public void cleanArgs(
            @NotNull String packageName,
            @NotNull String classSimpleName) {
        navArgs.cleanArgs(packageName, classSimpleName);
    }

    @NotNull
    public static File getMapJsonFile(@NotNull File generateDir) {
        return new File(generateDir, "nav-map.json");
    }

    @NotNull
    public static File getArgsJsonFile(@NotNull File generateDir) {
        return new File(generateDir, "nav-args.json");
    }

    @NotNull
    public static NavGenerator create(@NotNull CodeType codeType,
                                      String sourcePackageName,
                                      String navMapClassName,
                                      String navArgsClassName) {
        return fromCode(codeType,
                null,
                null,
                sourcePackageName,
                navMapClassName,
                navArgsClassName);
    }

    @NotNull
    public static NavGenerator fromFile(@NotNull CodeType codeType,
                                        File navMapJsonFile,
                                        File navArgsJsonFile,
                                        String sourcePackageName,
                                        String navMapClassName,
                                        String navArgsClassName) {
        String navMapJson = Utils.textFromFile(navMapJsonFile);
        String navArgsJson = Utils.textFromFile(navArgsJsonFile);

        return fromCode(codeType,
                navMapJson,
                navArgsJson,
                sourcePackageName,
                navMapClassName,
                navArgsClassName);
    }

    @NotNull
    public static NavGenerator fromCode(@NotNull CodeType codeType,
                                        String mapJson,
                                        String argsJson,
                                        String sourcePackageName,
                                        String navMapClassName,
                                        String navArgsClassName) {
        Parser parser = new ParserImpl();

        SourceFileGenerator generator;
        if (codeType == CodeType.Java) {
            generator = new JavaGenerator();
        } else if (codeType == CodeType.Kotlin) {
            generator = new KotlinGenerator();
        } else {
            throw new IllegalArgumentException(
                    "Unknown codeType: " + codeType);
        }

        navMapClassName = navMapClassName != null ?
                navMapClassName : DEF_NAV_MAP_CLASS_NAME;

        navArgsClassName = navArgsClassName != null ?
                navArgsClassName : DEF_NAV_ARGS_CLASS_NAME;

        sourcePackageName = sourcePackageName != null ?
                sourcePackageName : DEF_CLASS_PACKAGE;

        NavMap root = parser.parseMap(mapJson);
        if (root != null) {
            if (root.packageName == null ||
                    !root.packageName.equals(sourcePackageName)) {
                root.packageName = sourcePackageName;
            }

            if (root.name == null ||
                    !root.name.equals(navMapClassName)) {
                root.name = navMapClassName;
            }
        } else {
            root = new NavMap(
                    navMapClassName, true, sourcePackageName);
        }

        NavArgs navArgs = parser.parseArgs(argsJson);
        if (navArgs != null) {
            if (navArgs.sourcePackageName == null
                    || !navArgs.sourcePackageName.equals(sourcePackageName)) {
                navArgs.sourcePackageName = sourcePackageName;
            }

            if (navArgs.sourceClassName == null
                    || !navArgs.sourceClassName.equals(navArgsClassName)) {
                navArgs.sourceClassName = navArgsClassName;
            }
        } else {
            navArgs = new NavArgs(sourcePackageName, navArgsClassName);
        }

        return new NavGenerator(root, navArgs, parser, generator);
    }

    public enum CodeType {
        Java,
        Kotlin
    }
}
