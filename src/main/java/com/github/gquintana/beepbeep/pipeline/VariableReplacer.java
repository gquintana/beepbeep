package com.github.gquintana.beepbeep.pipeline;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableReplacer extends LineTransformer {
    private final Map<String, Object> variables;
    private final Pattern variablePattern;

    public VariableReplacer(Pattern variablePattern, Map<String, Object> variables, Consumer<ScriptEvent> consumer) {
        super(consumer);
        this.variablePattern = variablePattern;
        this.variables = variables;
    }

    public VariableReplacer(Map<String, Object> variables, Consumer<ScriptEvent> consumer) {
        this(Pattern.compile("\\$\\{([^\\}]+)\\}"), variables, consumer);
    }

    @Override
    protected String transform(String line) {
        return replace(line);
    }

    private String replace(String s) {
        Matcher matcher = variablePattern.matcher(s);
        int start = 0;
        StringBuilder r = new StringBuilder();
        while(matcher.find(start)) {
            if (matcher.start() > start) {
                r.append(s, start, matcher.start());
            }
            r.append(getVariable(matcher.group(1), matcher.group(0)));
            start = matcher.end();
        }
        if (start < s.length()) {
            r.append(s.substring(start));
        }
        return r.toString();
    }

    private String getVariable(String name, String complete) {
        Object value = variables.get(name);
        if (value == null) {
            return complete;
        } else if (value instanceof String) {
            return replace((String) value);
        } else {
            return value.toString();
        }
    }

}
