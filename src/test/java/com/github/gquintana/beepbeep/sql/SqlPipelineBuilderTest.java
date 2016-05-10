package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.ResultEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEndEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.ResourceScriptScanner;
import com.github.gquintana.beepbeep.script.ScriptScanners;
import com.github.gquintana.beepbeep.store.ScriptStore;
import org.h2.Driver;
import org.junit.After;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;

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

    @Test
    public void testConsume_ScriptStore() throws Exception {
        // Given
        TestConsumer output = new TestConsumer();
        SingleSqlConnectionProvider  connectionProvider= new SingleSqlConnectionProvider(DriverSqlConnectionProvider.create("jdbc:h2:mem:test", "sa", ""));
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider)
            .withVariable("variable", "value")
            .withScriptStore("beepbeep")
            .withEndConsumer(output);
        ScriptStore scriptStore = pipelineBuilder.getScriptStore();
        scriptStore.prepare();
        ResourceScriptScanner scriptScanner = ScriptScanners.resources(getClass().getClassLoader(),
            name -> name.startsWith("com/github/gquintana/beepbeep/script/") && name.endsWith(".sql"),
            pipelineBuilder.build()
        );
        scriptScanner.scan();
        output.clear();
        // When
        scriptScanner.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isEmpty();
        close(connectionProvider);
    }
    private static void close(SingleSqlConnectionProvider connectionProvider) throws Exception {
        try(Connection connection= connectionProvider.getConnection();
            Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE beepbeep");
        }
        connectionProvider.close();
    }

}
