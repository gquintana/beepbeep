package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

public class ScriptStartEvent extends ScriptEvent {

    public static final String TYPE = "START";

    public ScriptStartEvent(Script script) {
        super(TYPE, script);
    }
}
