package org.github.torand.openapi2java.utils;

public class StringHelper {
    private StringHelper() {}

    public static String pluralSuffix(int count) {
        return count == 1 ? "" : "s";
    }

    public static String capitalize(String name) {
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }
}
