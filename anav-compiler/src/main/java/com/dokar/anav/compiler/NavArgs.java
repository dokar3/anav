package com.dokar.anav.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NavArgs {

    public String sourcePackageName;

    public String sourceClassName;

    public final Map<String, Set<Arg>> args;

    public NavArgs(String sourcePackageName, String sourceClassName) {
        this.sourcePackageName = sourcePackageName;
        this.sourceClassName = sourceClassName;
        this.args = new HashMap<>();
    }

    public boolean isEmpty() {
        return args.isEmpty();
    }

    public void addArg(@NotNull String packageName,
                       @NotNull String classSimpleName,
                       @NotNull String argName,
                       @NotNull String argGroupName,
                       @Nullable String typeClassName) {
        if (typeClassName == null || typeClassName.isEmpty()) {
            // Use the default type
            typeClassName = "java.lang.String";
        }

        // as as canonicalName
        typeClassName = Utils.removeSuffix(typeClassName, ".class");

        final String className = packageName + "." + classSimpleName;

        final Arg arg = new Arg(className, argName, typeClassName);

        final Set<Arg> classArgs = args.computeIfAbsent(argGroupName,
                (key) -> new HashSet<>());
        // Remove old one
        classArgs.remove(arg);
        classArgs.add(arg);
    }

    public void cleanArgs(@NotNull String packageName,
                          @NotNull String classSimpleName) {
        final String className = packageName + "." + classSimpleName;
        final List<String> toRemove = new ArrayList<>();
        args.forEach((name, classArgs) -> {
            classArgs.removeIf(arg -> className.equals(arg.className));
            if (classArgs.isEmpty()) {
                toRemove.add(name);
            }
        });
        toRemove.forEach(args::remove);
    }

    public static class Arg {

        public String className;

        public String name;

        public String typeClassName;

        public Arg(
                String className,
                String name,
                String typeClassName) {
            this.className = className;
            this.name = name;
            this.typeClassName = typeClassName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Arg arg = (Arg) o;
            return Objects.equals(name, arg.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
