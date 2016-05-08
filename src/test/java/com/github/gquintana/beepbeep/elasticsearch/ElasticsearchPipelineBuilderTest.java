package com.github.gquintana.beepbeep.elasticsearch;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.http.HttpClientProvider;
import com.github.gquintana.beepbeep.http.HttpPipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.script.ResourceScriptScanner;
import com.github.gquintana.beepbeep.script.ScriptScanners;
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
    public void testGetHealth() {
        // Given
        TestConsumer output = new TestConsumer();
        HttpClientProvider clientProvider = new HttpClientProvider();
        Consumer<ScriptStartEvent> input = new HttpPipelineBuilder().withUrl(getElasticsearchUri())
            .withHttpClientProvider(clientProvider).withEndConsumer(output).build();
        // When
        input.consume(new ScriptStartEvent(ResourceScript.create(getClass(), "cluster_health.json")));
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
        TestConsumer output = new TestConsumer();
        HttpClientProvider clientProvider = new HttpClientProvider();
        Consumer input = new HttpPipelineBuilder()
            .withUrl(getElasticsearchUri())
            .withHttpClientProvider(clientProvider).withEndConsumer(output).build();
        // When
        String scriptGlob = getClass().getPackage().getName().replaceAll("\\.", "/") + "/index*.json";
        ResourceScriptScanner scanner = ScriptScanners.resources(getClass().getClassLoader(), scriptGlob, input);
        scanner.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).hasSize(3 * 2 + 1 + 4 + 1);
        assertThat(output.events.get(1).toString()).contains("200,OK");
    }
}
