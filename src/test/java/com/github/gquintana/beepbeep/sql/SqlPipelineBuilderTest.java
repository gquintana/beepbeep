package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ResultEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEndEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.Script;
import com.github.gquintana.beepbeep.script.ScriptScanners;
import org.h2.Driver;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.function.Predicate;

import static com.github.gquintana.beepbeep.sql.TestSqlConnectionProviders.createSqlConnectionProvider;
import static org.assertj.core.api.Assertions.assertThat;

public class SqlPipelineBuilderTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static void writeResource(ScriptStartEvent e, File scriptFolder) {
        try {
            Script s = e.getScript();
            TestFiles.writeResource("sql/init/" + s.getName(), new File(scriptFolder, s.getName()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        File scriptFolder = temporaryFolder.newFolder("script");
        ScriptScanners.resources(getClass().getClassLoader(), "com/github/gquintana/beepbeep/sql/init/*.sql",
            e -> writeResource(e, scriptFolder)).scan();
        DriverSqlConnectionProvider connectionProvider = createSqlConnectionProvider();
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider.getDriverClass().getName(), connectionProvider.getUrl(), connectionProvider.getUsername(), connectionProvider.getPassword())
            .withVariable("variable", "value")
            .withEndConsumer(output)
            .withFilesScriptScanner(scriptFolder.getPath() + "/*.sql");
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).hasSize(2 * 2 + 2 + 4);
        assertThat(output.events(ScriptStartEvent.class)).hasSize(2);
        assertThat(output.events(ScriptEndEvent.class)).hasSize(2);
        assertThat(output.events(ResultEvent.class)).hasSize(2 + 2 + 2);
    }

    @Test
    public void testConsume_ScriptStore() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        SingleSqlConnectionProvider connectionProvider = new SingleSqlConnectionProvider(createSqlConnectionProvider());
        Predicate<String> resourceFilter = name -> name.startsWith("com/github/gquintana/beepbeep/init/sql/") && name.endsWith(".sql");
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider)
            .withVariable("variable", "value")
            .withScriptStore("beepbeep")
            .withEndConsumer(output)
            .withResourcesScriptScanner(getClass().getClassLoader(), resourceFilter);
        pipelineBuilder.scan();
        output.clear();
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isEmpty();
        close(connectionProvider);
    }

    private static void close(SingleSqlConnectionProvider connectionProvider) throws Exception {
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE beepbeep");
            // When using sequences
            // statement.execute("DROP SEQUENCE beepbeep_seq");
        }
        connectionProvider.close();
    }

}
