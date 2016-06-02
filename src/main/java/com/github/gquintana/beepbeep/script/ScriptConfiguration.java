package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.util.Converters;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration/metadata read from script itself
 */
public class ScriptConfiguration {
    private final Map<String, Object> items = new HashMap<>();

    public <T> Optional<T> getValue(String name, Class<T> type) {
        return Optional.ofNullable(Converters.convert(items.get(name), type));
    }

    public <T> void setValue(String name, T value) {
        items.put(name, value);
    }

    private static final Pattern LINE_PATTERN = Pattern.compile("beepbeep((?:\\s+\\w+=[^\\s]+)*)");
    private static final Pattern VALUE_PATTERN = Pattern.compile("(\\w+)=([^\\s]+)");

    /**
     * Parse script line to extract configuration
     */
    public void parse(String line) {
        Matcher lineMatcher = LINE_PATTERN.matcher(line);
        if (lineMatcher.find()) {
            String values = lineMatcher.group(1);
            Matcher valueMatcher = VALUE_PATTERN.matcher(values);
            while(valueMatcher.find()) {
                setValue(valueMatcher.group(1), valueMatcher.group(2));
            }
        }
    }
}
