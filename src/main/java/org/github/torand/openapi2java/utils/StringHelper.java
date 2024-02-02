package org.github.torand.openapi2java.utils;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class StringHelper {
    private StringHelper() {}

    public static String pluralSuffix(int count) {
        return count == 1 ? "" : "s";
    }

    public static String capitalize(String value) {
        if (isBlank(value)) {
            return value;
        }
        return value.substring(0,1).toUpperCase() + value.substring(1);
    }

    public static String uncapitalize(String value) {
        if (isBlank(value)) {
            return value;
        }
        return value.substring(0,1).toLowerCase() + value.substring(1);
    }

    public static String stripHead(String value, int count) {
        if (isBlank(value)) {
            return value;
        }
        return value.substring(count);
    }

    public static String stripTail(String value, int count) {
        if (isBlank(value)) {
            return value;
        }
        return value.substring(0, Math.max(0, value.length()-count));
    }

    public static String removeLineBreaks(String value) {
        if (isBlank(value)) {
            return value;
        }
        return value.replaceAll("\\n", " ");
    }

    public static boolean isBlank(String value) {
        return isNull(value) || value.isEmpty();
    }

    public static boolean nonBlank(String value) {
        return nonNull(value) && !value.isEmpty();
    }
}
