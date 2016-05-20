package com.github.gquintana.beepbeep.util;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * String utilities
 */
public final class Strings {
    private Strings() {
    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

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

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toCamelCase(String key) {
        return Arrays.stream(key.split("[._-]"))
            .map(Strings::upperCaseFirstChar)
            .collect(Collectors.joining());
    }

    public static String upperCaseFirstChar(String word) {
        return word.isEmpty() ? "" :
            Character.toUpperCase(word.charAt(0)) + Strings.right(word, 1);
    }

}
