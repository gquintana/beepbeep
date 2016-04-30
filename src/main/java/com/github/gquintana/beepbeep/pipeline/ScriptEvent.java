package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

public class ScriptEvent {
    private final Script script;
    private final Type type;
    public enum Type {
        START, END_SUCCESS, END_FAILED
    }

    public ScriptEvent(Script script, Type type) {
        this.script = script;
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
