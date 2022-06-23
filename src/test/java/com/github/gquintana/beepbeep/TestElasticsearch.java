package com.github.gquintana.beepbeep;

import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public class TestElasticsearch {
    public static ElasticsearchContainer createContainer() {
        return new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch").withTag("7.17.4"));
    }
}
