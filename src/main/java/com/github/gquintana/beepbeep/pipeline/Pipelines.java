package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.elasticsearch.ElasticsearchPipelineBuilder;
import com.github.gquintana.beepbeep.http.HttpPipelineBuilder;
import com.github.gquintana.beepbeep.sql.SqlPipelineBuilder;
import org.kohsuke.args4j.CmdLineException;

/**
 * Pipeline factory
 */
public final class Pipelines {

    private Pipelines() {

    }

    public static PipelineBuilder create(String type) {
        PipelineBuilder pipelineBuilder;
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
                    throw new BeepBeepException("Invalid pipeline type " + type, e);
                }
        }
        return pipelineBuilder;
    }

}
