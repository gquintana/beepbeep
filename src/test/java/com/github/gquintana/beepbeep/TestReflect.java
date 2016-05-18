package com.github.gquintana.beepbeep;

import java.lang.reflect.Field;

public class TestReflect {
    /**
     * Get field value by reflection
     */
    public static Object getField(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = object.getClass();
        Field field = null;
        while (field == null && !clazz.equals(Object.class)) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = null;
            }
            clazz = clazz.getSuperclass();
        }
        if (field == null) {
            throw new NoSuchFieldException("Field " + fieldName + " not found");
        }
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        return field.get(object);
    }

}
