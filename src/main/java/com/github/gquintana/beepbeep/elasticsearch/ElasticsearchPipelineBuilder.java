package com.github.gquintana.beepbeep.elasticsearch;

import com.github.gquintana.beepbeep.http.HttpPipelineBuilder;

public class ElasticsearchPipelineBuilder extends HttpPipelineBuilder<ElasticsearchPipelineBuilder>  {
    @Override
    public ElasticsearchPipelineBuilder withScriptStore(String name) {
        return withScriptStore(new ElasticsearchScriptStore(getHttpClientProvider(), name));
    }
}
