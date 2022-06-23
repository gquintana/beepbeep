package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class PipelineBuilderTest {

    @Test
    public void testWithScriptReaderIgnoreErrors() throws Exception {
        // Given
        TestConsumer<ScriptEvent> out = ScriptReaderTest.failAtEventNum(2,4);
        PipelineBuilder pipelineBuilder = new PipelineBuilder() {
            @Override
            public PipelineBuilder withScriptStore(String name) {
                return null;
            }

            @Override
            public Consumer<ScriptStartEvent> createConsumers() {
                return scriptReader(endConsumer);
            }
        }.withEndConsumer(out)
            .withScriptReaderIgnoreErrors(true)
            .withResourceScriptScanner(TestFiles.class, "sql/init/02_data.sql");
        // When
        pipelineBuilder.scan();
        // Then
        assertThat(out.events).hasSize(6);
        assertThat(out.events(ResultEvent.class)).hasSize(2);
    }
}
