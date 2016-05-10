package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.store.ScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStoreFilter;
import com.github.gquintana.beepbeep.store.ScriptStoreUpdater;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public abstract class PipelineBuilder<B extends PipelineBuilder<B>> {
    protected Charset charset = Charset.forName("UTF-8");
    protected String url;
    protected String username;
    protected String password;
    protected ScriptStore scriptStore;
    private Map<String, Object> variables;
    protected Consumer<ScriptEvent> endConsumer = event -> {
    };

    protected B self() {
        return (B) this;
    }

    public B withEndConsumer(Consumer<ScriptEvent> endConsumer) {
        this.endConsumer = endConsumer;
        return self();
    }

    public B withCharset(Charset charset) {
        this.charset = charset;
        return self();
    }

    public B withVariables(Map<String, Object> variables) {
        if (this.variables == null) {
            this.variables = new HashMap<>(variables);
        } else {
            this.variables.putAll(variables);
        }
        return self();
    }

    public B withVariable(String name, Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name, value);
        return self();
    }

    public B withUrl(String url) {
        this.url = url;
        return self();
    }

    public B withUsername(String username) {
        this.username = username;
        return self();
    }

    public B withPassword(String password) {
        this.password = password;
        return self();
    }

    public ScriptStore getScriptStore() {
        return scriptStore;
    }

    public B withScriptStore(ScriptStore scriptStore) {
        this.scriptStore = scriptStore;
        return self();
    }

    public abstract B withScriptStore(String name);

    protected Consumer<ScriptEvent> notNullNorEmptyFilter(Consumer<ScriptEvent> consumer) {
        return LineFilter.notNulNotEmptyFilter(consumer);
    }

    protected Consumer<ScriptEvent> variableReplacer(Consumer<ScriptEvent> consumer) {
        if (variables != null && !variables.isEmpty()) {
            consumer = new VariableReplacerProcessor(variables, consumer);
        }
        return consumer;
    }

    protected Consumer<ScriptStartEvent> scriptReader(Consumer<ScriptEvent> consumer) {
        if (scriptStore != null) {
            consumer = new ScriptStoreUpdater<>(scriptStore, consumer);
        }
        Consumer<ScriptStartEvent> startConsumer = new ScriptReaderProducer(consumer, charset);
        if (scriptStore != null) {
            startConsumer = new ScriptStoreFilter(scriptStore, startConsumer);
        }
        return startConsumer;
    }

    public abstract Consumer build();
}
