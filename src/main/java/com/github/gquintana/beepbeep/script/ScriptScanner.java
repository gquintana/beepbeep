package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

import java.io.IOException;

public abstract class ScriptScanner {
    private final Consumer<Script> scriptConsumer;

    protected ScriptScanner(Consumer<Script> scriptConsumer) {
        this.scriptConsumer = scriptConsumer;
    }

    protected void produce(Script script) {
        scriptConsumer.consume(script);
    }

    public abstract void scan() throws IOException;
}
