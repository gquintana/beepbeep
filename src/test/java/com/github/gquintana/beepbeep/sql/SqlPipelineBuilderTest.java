package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.ResultEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEndEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.ResourceScriptScanner;
import com.github.gquintana.beepbeep.script.ScriptScanners;
import org.h2.Driver;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class SqlPipelineBuilderTest {
    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer output = new TestConsumer();
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(Driver.class.getName(), "jdbc:h2:mem:test", "sa", "")
            .withVariable("variable", "value")
            .withEndConsumer(output);
        ResourceScriptScanner scriptScanner = ScriptScanners.resources(getClass().getClassLoader(),
            name -> name.startsWith("com/github/gquintana/beepbeep/script/") && name.endsWith(".sql"),
            pipelineBuilder.build()
        );
        // When
        scriptScanner.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).hasSize(3 * 2 + 2 + 4 + 1);
        assertThat(output.events(ScriptStartEvent.class)).hasSize(3);
        assertThat(output.events(ScriptEndEvent.class)).hasSize(3);
        assertThat(output.events(ResultEvent.class)).hasSize(2 + 2 + 2 + 1);
    }

}
