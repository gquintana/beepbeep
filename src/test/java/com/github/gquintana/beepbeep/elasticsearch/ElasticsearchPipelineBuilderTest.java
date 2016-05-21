package com.github.gquintana.beepbeep.elasticsearch;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.http.BasicHttpClientProvider;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticsearchPipelineBuilderTest {
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ElasticsearchRule elasticsearch = new ElasticsearchRule(temporaryFolder);

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

    String getElasticsearchUri() {
        return "http://" + elasticsearch.getElasticsearch().getHttpAddress();
    }

    @Test
    public void testCreateSearchDelete() throws IOException {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        BasicHttpClientProvider clientProvider = new BasicHttpClientProvider();
        String scriptGlob = getClass().getPackage().getName().replaceAll("\\.", "/") + "/index*.json";
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
        String scriptGlob = getClass().getPackage().getName().replaceAll("\\.", "/") + "/index*.json";
        ElasticsearchPipelineBuilder pipelineBuilder = new ElasticsearchPipelineBuilder()
            .withUrl(getElasticsearchUri())
            .withHttpClientProvider(clientProvider).withEndConsumer(output)
            .withScriptStore(".beepbeep/script")
            .withResourcesScriptScanner(Thread.currentThread().getContextClassLoader(), scriptGlob);
        pipelineBuilder.scan();
        output.clear();
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isEmpty();
    }
}
