package com.github.t1.swap;

public class Helpers {
    public static String nonEmpty(String string) {
        return (string.isEmpty()) ? null : string;
    }

    public static String deprefixed(String value) {
        if (value.startsWith("/"))
            return value.substring(1);
        return value;
    }

    public static String prefixedPath(String value) {
        if (!value.startsWith("/"))
            value = "/" + value;
        return value;
    }
}
