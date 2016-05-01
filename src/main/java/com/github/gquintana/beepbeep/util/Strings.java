package com.github.gquintana.beepbeep.util;

/**
 * String utilities
 */
public final class Strings {
    private Strings() {
    }

    public static boolean isNullOrEmpty(String line) {
        return line == null || line.trim().isEmpty();
    }

    public static boolean isNotNullNorEmpty(String line) {
        return !isNullOrEmpty(line);
    }

    public static String left(String line, int end) {
        if (end >= line.length()) {
            return line;
        }
        if (end <= 0) {
            return "";
        }
        return line.substring(0, end);
    }

    public static String right(String line, int start) {
        if (start >= line.length()) {
            return "";
        }
        if (start <= 0) {
            return line;
        }
        return line.substring(start, line.length());
    }
}
