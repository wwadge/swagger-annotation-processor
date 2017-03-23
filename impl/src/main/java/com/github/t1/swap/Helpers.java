package com.github.t1.swap;

public class Helpers {
    private Helpers() {}

    public static String nonEmpty(String string) { return (string.isEmpty()) ? null : string; }

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

    /** @return the first sentence of the javadoc comment, or all, if it contains no period */
    public static String summary(String javaDoc) {
        int firstPeriod = javaDoc.indexOf('.');
        return (firstPeriod < 0) ? javaDoc : javaDoc.substring(0, firstPeriod);
    }
}
