package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

public class SingleScriptScanner extends ScriptScanner {
    private final Script script;

    public SingleScriptScanner(Script script, Consumer<ScriptStartEvent> scriptConsumer) {
        super(scriptConsumer);
        this.script = script;
    }

    @Override
    public void scan() {
        produce(script);
    }
}
