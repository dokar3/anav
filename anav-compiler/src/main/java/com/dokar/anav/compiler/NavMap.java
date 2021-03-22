package com.dokar.anav.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavMap {
    @Nullable
    public String packageName;

    // class name
    public String name;

    public final boolean isRoot;

    // fields: [fieldName, Destination]
    public Map<String, Dest> destinations;

    // sub classes: [classSimpleName, Group]
    public Map<String, NavMap> subGroups;

    public NavMap(
            @NotNull String name,
            boolean isRoot,
            @Nullable String packageName) {
        if (isRoot && packageName == null) {
            throw new IllegalArgumentException(
                    "Root group must have a package name");
        }

        this.name = name;
        this.isRoot = isRoot;
        this.packageName = packageName;
        destinations = new HashMap<>();
        subGroups = new HashMap<>();
    }

    public void addDestination(@NotNull Dest dest) {
        if (subGroups.containsKey(dest.name)) {
            System.err.println("There is a group has same name " +
                    "as this destination name: " + dest.name);
            return;
        }
        destinations.put(dest.name, dest);
    }

    public void addDestination(
            @NotNull String name,
            @NotNull String packageName,
            @NotNull String classSimpleName,
            @Nullable String groupName) {
        String className = packageName + "." + classSimpleName;
        Dest dest = new Dest(name, className);
        NavMap targetGroup = getOrCreateGroup(groupName);

        targetGroup.addDestination(dest);
    }

    public void cleanDestination(
            @NotNull String packageName,
            @NotNull String classSimpleName) {
        cleanDestination(this, packageName, classSimpleName);
    }

    private void cleanDestination(
            @NotNull NavMap navMap,
            @NotNull String packageName,
            @NotNull String classSimpleName) {
        String className = packageName + "." + classSimpleName;

        navMap.destinations.entrySet().removeIf(entry ->
                className.equals(entry.getValue().className)
        );

        List<String> toRemove = new ArrayList<>();
        navMap.subGroups.forEach((name, sub) -> {
            cleanDestination(sub, packageName, classSimpleName);
            if (sub.destinations.isEmpty()) {
                toRemove.add(name);
            }
        });
        toRemove.forEach(navMap.subGroups::remove);
    }

    private NavMap getOrCreateGroup(@Nullable String groupName) {
        if (groupName == null ||
                groupName.length() == 0 ||
                groupName.equals(name)) {
            return this;
        }

        if (subGroups.containsKey(groupName)) {
            return subGroups.get(groupName);
        }

        NavMap subGroup = new NavMap(groupName,
                false, packageName);
        subGroups.put(groupName, subGroup);

        return subGroup;
    }

    public static class Dest {
        // field name
        public final String name;

        // field value = "className"
        public final String className;

        public Dest(
                @NotNull String name,
                @NotNull String className) {
            this.name = name;
            this.className = className;
        }
    }
}