package com.github.gquintana.beepbeep.elasticsearch;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class ElasticsearchRule extends ExternalResource {
    private final TemporaryFolder temporaryFolder;
    private EmbeddedElasticsearch elasticsearch;

    public ElasticsearchRule(TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder;
    }

    @Override
    protected void before() throws Throwable {
        elasticsearch = new EmbeddedElasticsearch(temporaryFolder.newFolder("elasticsearch"));
        elasticsearch.start();
    }

    public EmbeddedElasticsearch getElasticsearch() {
        return elasticsearch;
    }

    @Override
    protected void after() {
        elasticsearch.close();
    }
}
