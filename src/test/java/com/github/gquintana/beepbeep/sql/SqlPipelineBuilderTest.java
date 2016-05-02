package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptReaderProducer;
import com.github.gquintana.beepbeep.script.ResourceScriptScanner;
import com.github.gquintana.beepbeep.script.ScriptScanners;
import org.h2.Driver;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.*;

public class SqlPipelineBuilderTest {
    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer consumer = new TestConsumer();
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(Driver.class.getName(), "jdbc:h2:mem:test", "sa", "")
            .withVariable("variable", "value")
            .withEndConsumer(consumer);
        ResourceScriptScanner scriptScanner = ScriptScanners.resources(getClass().getClassLoader(),
            name -> name.startsWith("com/github/gquintana/beepbeep/") && name.endsWith(".sql"),
            pipelineBuilder.build()
        );
        // When
        scriptScanner.scan();
        // Then
        assertThat(consumer.events).hasSize(10);
        assertThat(consumer.events(ScriptEvent.class)).hasSize(2 * 3);
        assertThat(consumer.events(String.class)).hasSize(2+2);
    }

}
