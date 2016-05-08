package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

public class ScriptEvent {
    private final String type;
    private final Script script;

    public ScriptEvent(String type, Script script) {
        this.type = type;
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = toStringBuilder();
        return stringBuilder.toString();
    }

    protected StringBuilder toStringBuilder() {
        StringBuilder stringBuilder = new StringBuilder().append(type);
        if (script != null) {
            stringBuilder.append(" ").append(script.getFullName());
        }
        return stringBuilder;
    }
}
