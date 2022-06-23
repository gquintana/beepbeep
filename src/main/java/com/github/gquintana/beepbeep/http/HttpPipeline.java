package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.pipeline.Pipeline;
import com.github.gquintana.beepbeep.script.ScriptScanner;
import com.github.gquintana.beepbeep.store.ScriptStore;

public class HttpPipeline extends Pipeline {
    protected final HttpClientProvider clientProvider;

    public HttpPipeline(ScriptStore scriptStore, ScriptScanner scriptScanner, HttpClientProvider clientProvider) {
        super(scriptStore, scriptScanner);
        this.clientProvider = clientProvider;
    }

}
