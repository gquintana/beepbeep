package com.github.gquintana.beepbeep.pipeline;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public abstract class PipelineBuilder<B extends PipelineBuilder<B>> {
    protected Charset charset = Charset.forName("UTF-8");
    protected String url;
    protected String username;
    protected String password;

    private Map<String, Object> variables;
    protected Consumer endConsumer = event -> {
    };

    public B withEndConsumer(Consumer endConsumer) {
        this.endConsumer = endConsumer;
        return (B) this;
    }

    public B withCharset(Charset charset) {
        this.charset = charset;
        return (B) this;
    }

    public B withVariables(Map<String, Object> variables) {
        if (this.variables == null) {
            this.variables = new HashMap<>(variables);
        } else {
            this.variables.putAll(variables);
        }
        return (B) this;
    }

    public B withVariable(String name, Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name, value);
        return (B) this;
    }

    public B withUrl(String url) {
        this.url = url;
        return (B) this;
    }

    public B withUsername(String username) {
        this.username = username;
        return (B) this;
    }

    public B withPassword(String password) {
        this.password = password;
        return (B) this;
    }


    protected Consumer notNullNorEmptyFilter(Consumer consumer) {
        return LineFilter.notNulNotEmptyFilter(consumer);
    }

    protected Consumer variableReplacer(Consumer consumer) {
        if (variables != null && !variables.isEmpty()) {
            consumer = new VariableReplacerProcessor(variables, consumer);
        }
        return consumer;
    }

    protected Consumer scriptReader(Consumer consumer) {
        return new ScriptReaderProducer(consumer, charset);
    }

    public abstract Consumer build();
}
