package com.github.gquintana.beepbeep.util;

import com.github.gquintana.beepbeep.config.ConfigurationException;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

public class Converters {
    /**
     * Convert object to appropriate type
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object object, Class<T> clazz) {
        Object result;
        if (object == null || clazz.isInstance(object)) {
            result = object;
        } else if (clazz.equals(String.class)) {
            result = convertToString(object);
        } else if (clazz.equals(Charset.class)) {
            result = convertToCharset(object);
        } else if (clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE)) {
            result = convertToBoolean(object);
        } else if (clazz.equals(Integer.class) || clazz.equals(Integer.TYPE)) {
            result = convertToInteger(object);
        } else if (clazz.equals(Long.class) || clazz.equals(Long.TYPE)) {
            result = convertToLong(object);
        } else if (clazz.equals(TemporalAmount.class) || clazz.equals(Duration.class)) {
            result = convertToDuration(object);
        } else if (clazz.equals(Instant.class)) {
            result = convertToInstant(object);
        } else if (Enum.class.isAssignableFrom(clazz)) {
            result = convertToEnum(object, (Class<Enum>) clazz);
        } else {
            throw new ConfigurationException("Don't known how to convert to " + clazz);
        }
        return clazz.isPrimitive() ? (T) result : clazz.cast(result);
    }


    /**
     * Convert object to string
     */
    public static String convertToString(Object object) {
        String result;
        if (object == null || object instanceof String) {
            result = (String) object;
        } else {
            result = object.toString();
        }
        return Strings.trimToNull(result);
    }

    /**
     * Convert object to boolean
     */
    private static Boolean convertToBoolean(Object object) {
        String name = convertToString(object);
        return name == null ? null : Boolean.valueOf(name.toLowerCase());
    }

    /**
     * Convert object to integer
     */
    private static Integer convertToInteger(Object object) {
        String name = convertToString(object);
        return name == null ? null : Integer.valueOf(name.toLowerCase());
    }

    /**
     * Convert object to long
     */
    private static Long convertToLong(Object object) {
        String name = convertToString(object);
        return name == null ? null : Long.valueOf(name.toLowerCase());
    }

    /**
     * Convert object to enum
     */
    private static <E extends Enum<E>> E convertToEnum(Object object, Class<E> enumClass) {
        String name = convertToString(object);
        return name == null ? null : Enum.valueOf(enumClass, name.toUpperCase());
    }

    /**
     * Convert object to instant
     */
    private static Instant convertToInstant(Object object) {
        String name = convertToString(object);
        return name == null ? null : Instant.parse(name);
    }

    /**
     * Convert object to duration
     */
    private static Duration convertToDuration(Object object) {
        String name = convertToString(object);
        return name == null ? null : Duration.parse("PT" + convertToString(object).toUpperCase());
    }

    /**
     * Convert object to charset
     */
    private static Charset convertToCharset(Object object) {
        String name = convertToString(object);
        return name == null ? null : Charset.forName(convertToString(object));
    }
}
