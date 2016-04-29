package com.github.gquintana.dbscriptrunner.pipeline;

public class ScriptEvent {
    private final Type type;
    public enum Type {
        START, END_SUCCESS, END_FAILED
    }

    public ScriptEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
