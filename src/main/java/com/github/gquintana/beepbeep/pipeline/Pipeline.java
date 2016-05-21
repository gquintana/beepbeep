package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.ScriptScanner;
import com.github.gquintana.beepbeep.store.ScriptStore;

import java.io.IOException;

public class Pipeline implements AutoCloseable {
    protected final ScriptStore scriptStore;
    protected final ScriptScanner scriptScanner;

    public Pipeline(ScriptStore scriptStore, ScriptScanner scriptScanner) {
        this.scriptStore = scriptStore;
        this.scriptScanner = scriptScanner;
    }

    public void run() throws IOException {
        // Prepare script store if needed
        if (scriptStore != null) {
            scriptStore.prepare();
        }
        // Run script scanner
        scriptScanner.scan();
    }

    @Override
    public void close() {

    }
}
