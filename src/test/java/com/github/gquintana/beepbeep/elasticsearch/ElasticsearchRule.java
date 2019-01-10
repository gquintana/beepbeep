package com.github.gquintana.beepbeep.elasticsearch;

import org.junit.rules.ExternalResource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class ElasticsearchRule extends ExternalResource {
    private final ElasticsearchContainer container;

    public ElasticsearchRule() {
        container = new ElasticsearchContainer();
    }

    @Override
    protected void before() throws Throwable {
        container.start();
    }

    public String getHttpHostAddress() {
        return "http://" + container.getHttpHostAddress();
    }

    @Override
    protected void after() {
        container.stop();
    }
}
