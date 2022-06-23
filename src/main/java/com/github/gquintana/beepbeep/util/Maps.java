package com.github.gquintana.beepbeep.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Map} utilities
 */
public final class Maps {
    private Maps() {
    }

    @SuppressWarnings("unchecked")
    private static void flatten(Object object, String prefix, Map<String, Object> collector) {
        String longPrefix = prefix == null ? "" : prefix + ".";
        if (object instanceof Map) {
            Map<String, ?> map = (Map<String, ?>) object;
            for (Map.Entry<String, ?> mapEntry : map.entrySet()) {
                flatten(mapEntry.getValue(), longPrefix + mapEntry.getKey(), collector);
            }
        } else if (object instanceof Collection) {
            Collection<?> coll = (Collection<?>) object;
            int index = 0;
            for (Object value : coll) {
                flatten(value, longPrefix + index, collector);
                index++;
            }
        } else {
            collector.put(prefix, object);
        }
    }

    /**
     * Converts a map <code>{ k1:{k11:v11, k12:v12}, k2:v2}</code> into a map
     * <code>/code><{ k1.k11:v11, k1.k12:v12, k2:v2}</code>
     */
    public static Map<String, Object> flatten(Object object, String prefix) {
        Map<String, Object> collector = new HashMap<>();
        flatten(object, prefix, collector);
        return collector;
    }


    /**
     * Helper to fill a Map
     */
    public static <K, V> Builder<K, V> builder(Map<K, V> map) {
        return new Builder<>(map);
    }

    /**
     * Helper to fill a Map
     */
    public static <K, V> Builder<K, V> builder() {
        return builder(new HashMap<>());
    }

    public static class Builder<K, V> {
        private final Map<K, V> map;

        private Builder(Map<K, V> map) {
            this.map = map;
        }

        public Builder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return map;
        }
    }
}
