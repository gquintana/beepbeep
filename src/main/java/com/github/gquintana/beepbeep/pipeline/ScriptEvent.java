package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptEvent that = (ScriptEvent) o;
        return Objects.equals(type, that.type) &&
            Objects.equals(script, that.script);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, script);
    }
}
