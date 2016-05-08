package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

public class ScriptEvent {
    private final Script script;
    private final Type type;
    private final Exception exception;
    public enum Type {
        START, END_SUCCESS, END_FAILED
    }

    public ScriptEvent(Script script, Type type) {
        this(script, type, null);
    }

    public ScriptEvent(Script script, Type type, Exception exception) {
        this.script = script;
        this.type = type;
        this.exception = exception;
    }

    public Type getType() {
        return type;
    }

    public Script getScript() {
        return script;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append(type).append(" ").append(script.getFullName());
        if (exception != null) {
            stringBuilder.append(" ").append(exception.getMessage());
        }
        return stringBuilder.toString();
    }
}
