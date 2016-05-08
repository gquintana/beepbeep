package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.pipeline.*;

public class HttpPipelineBuilder extends PipelineBuilder<HttpPipelineBuilder> {
    private HttpClientProvider httpClientProvider = new HttpClientProvider();

    public HttpPipelineBuilder withHttpClientProvider(HttpClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
        return this;
    }

    public Consumer<ScriptStartEvent> build() {
        Consumer<ScriptEvent> consumer = endConsumer;
        httpClientProvider.setUrl(url);
        httpClientProvider.setUsername(username);
        httpClientProvider.setPassword(password);
        consumer = new HttpLineExecutor(httpClientProvider, consumer);
        consumer = notNullNorEmptyFilter(consumer);
        consumer = new MultilineAggregator("^\\s*(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH)\\s+", MultilineAggregator.LineMarkerStrategy.START, false, consumer);
        consumer = variableReplacer(consumer);
        return scriptReader(consumer);
    }
}
