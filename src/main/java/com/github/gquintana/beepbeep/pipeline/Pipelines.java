package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.config.ConfigurationException;
import com.github.gquintana.beepbeep.elasticsearch.ElasticsearchPipelineBuilder;
import com.github.gquintana.beepbeep.http.HttpPipelineBuilder;
import com.github.gquintana.beepbeep.sql.SqlPipelineBuilder;

/**
 * Pipeline factory
 */
public final class Pipelines {

    private Pipelines() {

    }

    public static PipelineBuilder create(String type) {
        PipelineBuilder pipelineBuilder;
        if (type == null) {
            throw new ConfigurationException("Null pipeline type");
        }
        switch (type) {
            case "sql":
                pipelineBuilder = new SqlPipelineBuilder();
                break;
            case "http":
                pipelineBuilder = new HttpPipelineBuilder();
                break;
            case "elasticsearch":
                pipelineBuilder = new ElasticsearchPipelineBuilder();
                break;
            default:
                try {
                    pipelineBuilder = (PipelineBuilder) Class.forName(type).newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new ConfigurationException("Invalid pipeline type " + type, e);
                }
        }
        return pipelineBuilder;
    }

}
