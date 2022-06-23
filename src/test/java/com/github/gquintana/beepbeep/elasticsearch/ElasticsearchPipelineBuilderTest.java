package com.github.gquintana.beepbeep.elasticsearch;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestElasticsearch;
import com.github.gquintana.beepbeep.http.BasicHttpClientProvider;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class ElasticsearchPipelineBuilderTest {
    @TempDir
    Path tempDir;
    @Container
    static ElasticsearchContainer elasticsearchContainer = TestElasticsearch.createContainer();

    @Test
    public void testGetHealth() throws IOException {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        BasicHttpClientProvider clientProvider = new BasicHttpClientProvider();
        ElasticsearchPipelineBuilder pipelineBuilder = new ElasticsearchPipelineBuilder().withUrl(getElasticsearchUri())
            .withHttpClientProvider(clientProvider)
            .withEndConsumer(output)
            .withResourceScriptScanner(getClass(), "cluster_health.json");
        // When
        pipelineBuilder.scan();
        // Then
        assertThat(output.events).hasSize(3);
        assertThat(output.events.get(1).toString()).contains("200,OK");
    }

    private String getElasticsearchUri() {
        return "http://" + elasticsearchContainer.getHttpHostAddress();
    }

    @Test
    public void testCreateSearchDelete() throws IOException {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        BasicHttpClientProvider clientProvider = new BasicHttpClientProvider();
        String scriptGlob = getJsonScripts();
        ElasticsearchPipelineBuilder pipelineBuilder = new ElasticsearchPipelineBuilder()
            .withUrl(getElasticsearchUri())
            .withHttpClientProvider(clientProvider)
            .withEndConsumer(output)
            .withResourcesScriptScanner(getClass().getClassLoader(), scriptGlob);
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).hasSize(3 * 2 + 1 + 4 + 1);
        assertThat(output.events.get(1).toString()).contains("200,OK");
    }
    @Test
    public void testStore() throws IOException {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        BasicHttpClientProvider clientProvider = new BasicHttpClientProvider();
        String scriptGlob = getJsonScripts();
        ElasticsearchPipelineBuilder pipelineBuilder = new ElasticsearchPipelineBuilder()
            .withUrl(getElasticsearchUri())
            .withHttpClientProvider(clientProvider).withEndConsumer(output)
            .withScriptStore(".beepbeep")
            .withResourcesScriptScanner(Thread.currentThread().getContextClassLoader(), scriptGlob);
        pipelineBuilder.scan();
        output.clear();
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isEmpty();
    }

    @Test
    public void testMemStore() throws IOException {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        BasicHttpClientProvider clientProvider = new BasicHttpClientProvider();
        String scriptGlob = getJsonScripts();
        ElasticsearchPipelineBuilder pipelineBuilder = new ElasticsearchPipelineBuilder()
            .withUrl(getElasticsearchUri())
            .withHttpClientProvider(clientProvider).withEndConsumer(output)
            .withScriptStore("mem:test")
            .withResourcesScriptScanner(Thread.currentThread().getContextClassLoader(), scriptGlob);
        pipelineBuilder.scan();
        output.clear();
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isEmpty();
    }

    private String getJsonScripts() {
        return getClass().getPackage().getName().replaceAll("\\.", "/") + "/index*.json";
    }
}
