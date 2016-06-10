package com.github.gquintana.beepbeep.elasticsearch;

import com.github.gquintana.beepbeep.http.HttpPipelineBuilder;
import com.github.gquintana.beepbeep.script.ScriptStores;
import com.github.gquintana.beepbeep.store.ScriptStore;

public class ElasticsearchPipelineBuilder extends HttpPipelineBuilder<ElasticsearchPipelineBuilder>  {
    @Override
    public ElasticsearchPipelineBuilder withScriptStore(String name) {
        ScriptStore store = ScriptStores.scheme(name);
        if (store == null) {
            return withScriptStore(new ElasticsearchScriptStore(getHttpClientProvider(), name));
        } else {
            return withScriptStore(store);
        }
    }
}
