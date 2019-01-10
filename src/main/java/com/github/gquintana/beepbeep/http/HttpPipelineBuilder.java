package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.MultilineAggregator;
import com.github.gquintana.beepbeep.pipeline.Pipeline;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.store.MemoryScriptStore;

import java.util.ArrayList;
import java.util.List;

public class HttpPipelineBuilder<B extends HttpPipelineBuilder<B>> extends PipelineBuilder<B> {
    private HttpClientProvider httpClientProvider = new BasicHttpClientProvider();
    private List<HttpHeader> defaultHttpHeaders;

    public B withHttpClientProvider(HttpClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
        return self();
    }

    public B withDefaultHttpHeader(String name, String value) {
        if (defaultHttpHeaders == null) {
            defaultHttpHeaders = new ArrayList<>();
        }
        defaultHttpHeaders.add(new HttpHeader(name, value));
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
        consumer = new HttpLineExecutor(getHttpClientProvider(), consumer, defaultHttpHeaders);
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
