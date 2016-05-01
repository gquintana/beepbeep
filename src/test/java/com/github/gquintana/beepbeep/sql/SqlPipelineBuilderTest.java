package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.ScriptReaderProducer;
import com.github.gquintana.beepbeep.script.ResourceScriptScanner;
import org.h2.Driver;
import org.junit.Test;

import java.nio.charset.Charset;

public class SqlPipelineBuilderTest {
    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer consumer = new TestConsumer();
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(Driver.class.getName(), "jdbc:h2:mem:test", "sa", "")
            .withVariable("variable", "value")
            .withEndConsumer(consumer);
        ResourceScriptScanner scriptScanner = new ResourceScriptScanner(getClass().getClassLoader(),
            name -> name.startsWith("com/github/gquintana/beepbeep/") && name.endsWith(".sql"),
            pipelineBuilder.build()
        );
        // When
        scriptScanner.scan();
    }

}
