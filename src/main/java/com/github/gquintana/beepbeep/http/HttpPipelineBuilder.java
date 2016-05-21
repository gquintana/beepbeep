package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.pipeline.*;
import com.github.gquintana.beepbeep.store.MemoryScriptStore;

public class HttpPipelineBuilder<B extends HttpPipelineBuilder<B>> extends PipelineBuilder<B> {
    private HttpClientProvider httpClientProvider = new BasicHttpClientProvider();

    public B withHttpClientProvider(HttpClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
        return self();
    }

    public HttpClientProvider getHttpClientProvider() {
        if (httpClientProvider instanceof BasicHttpClientProvider) {
            BasicHttpClientProvider baseHttpClientProvider = (BasicHttpClientProvider) httpClientProvider;
            baseHttpClientProvider.setUrl(url);
            baseHttpClientProvider.setUsername(username);
            baseHttpClientProvider.setPassword(password);
            return baseHttpClientProvider;
        } else {
            return httpClientProvider;
        }
    }

    @Override
    public B withScriptStore(String name) {
        return withScriptStore(new MemoryScriptStore());
    }

    @Override
    public Consumer<ScriptStartEvent> createConsumers() {
        Consumer<ScriptEvent> consumer = endConsumer;
        consumer = new HttpLineExecutor(getHttpClientProvider(), consumer);
        consumer = notNullNorEmptyFilter(consumer);
        consumer = new MultilineAggregator("^\\s*(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH)\\s+", MultilineAggregator.LineMarkerStrategy.START, false, consumer);
        consumer = variableReplacer(consumer);
        return scriptReader(consumer);
    }

    @Override
    public Pipeline build() {
        return new HttpPipeline(getScriptStore(), createScriptScanner(), getHttpClientProvider());
    }
}
