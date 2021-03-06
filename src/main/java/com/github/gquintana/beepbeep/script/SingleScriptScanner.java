package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

import java.io.IOException;

public class SingleScriptScanner extends ScriptScanner {
    private final Script script;

    public SingleScriptScanner(Script script, Consumer<ScriptStartEvent> scriptConsumer) {
        super(scriptConsumer);
        this.script = script;
    }

    @Override
    public void scan() throws IOException {
        produce(script);
    }
}
