package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.MultilineAggregator;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;

public class HttpPipelineBuilder extends PipelineBuilder<HttpPipelineBuilder> {
    private HttpClientProvider httpClientProvider;

    public HttpPipelineBuilder withHttpClientProvider(HttpClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
        return this;
    }

    public Consumer build() {
        Consumer consumer = endConsumer;
        consumer = new HttpLineExecutor(httpClientProvider, consumer);
        consumer = notNullNorEmptyFilter(consumer);
        consumer = new MultilineAggregator("^\\s*(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH)\\s+", MultilineAggregator.LineMarkerStrategy.START, false, consumer);
        consumer = variableReplacer(consumer);
        consumer = scriptReader(consumer);
        return consumer;
    }
}
