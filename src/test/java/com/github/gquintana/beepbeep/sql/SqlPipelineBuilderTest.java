package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.LineException;
import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ResultEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEndEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.Script;
import com.github.gquintana.beepbeep.script.ScriptScanners;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.gquintana.beepbeep.sql.TestSqlConnectionProviders.createSqlConnectionProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SqlPipelineBuilderTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private boolean store = false;

    private static void writeResource(ScriptStartEvent e, File scriptFolder) {
        try {
            Script s = e.getScript();
            TestFiles.writeResource("sql/init/" + s.getName(), new File(scriptFolder, s.getName()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public DriverSqlConnectionProvider createFileConnectionProvider() throws IOException {
        File h2dbFolder = temporaryFolder.newFolder("h2db");
        return createSqlConnectionProvider(h2dbFolder);
    }
    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        File scriptFolder = temporaryFolder.newFolder("script");
        ScriptScanners.resources(getClass().getClassLoader(), "com/github/gquintana/beepbeep/sql/init/*.sql",
            e -> writeResource(e, scriptFolder)).scan();
        DriverSqlConnectionProvider connectionProvider = createFileConnectionProvider();
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
        close(connectionProvider);
    }

    private static boolean isInitSqlScript(String name) {
        return name.startsWith("com/github/gquintana/beepbeep/init/sql/") && name.endsWith(".sql");
    }

    @Test
    public void testConsume_ScriptStore() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        SingleSqlConnectionProvider connectionProvider = createSqlConnectionProvider().single();
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider)
            .withScriptStore("beepbeep")
            .withEndConsumer(output)
            .withResourcesScriptScanner(getClass().getClassLoader(), SqlPipelineBuilderTest::isInitSqlScript);
        store = true;
        pipelineBuilder.scan();
        output.clear();
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isEmpty();
        close(connectionProvider);
    }

    @Test
    public void testConsume_FileScriptStore() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        SingleSqlConnectionProvider connectionProvider = createSqlConnectionProvider().single();
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider)
            .withScriptStore(temporaryFolder.newFile("store.yml").toURI().toString())
            .withEndConsumer(output)
            .withResourcesScriptScanner(getClass().getClassLoader(), SqlPipelineBuilderTest::isInitSqlScript);
        store = true;
        pipelineBuilder.scan();
        output.clear();
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isEmpty();
        close(connectionProvider);
    }

    @Test
    public void testConsume_Variable() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        SingleSqlConnectionProvider connectionProvider = new SingleSqlConnectionProvider(createSqlConnectionProvider());
        Predicate<String> resourceFilter = name -> name.startsWith("com/github/gquintana/beepbeep/sql/init_vars/") && name.endsWith(".sql");
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider)
            .withVariable("person.login", "wile")
            .withVariable("person.email", "wile.coyote@warnerbros.com")
            .withEndConsumer(output)
            .withResourcesScriptScanner(getClass().getClassLoader(), resourceFilter);
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isNotEmpty();
        assertThat(output.eventStream(ResultEvent.class).map(ResultEvent::getResult).collect(Collectors.toSet())).contains("0;wile;wile.coyote@warnerbros.com");
        close(connectionProvider);
    }

    @Test
    public void testConsume_Variables() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        SingleSqlConnectionProvider connectionProvider = createSqlConnectionProvider().single();
        Predicate<String> resourceFilter = name -> name.startsWith("com/github/gquintana/beepbeep/sql/init_vars/") && name.endsWith(".sql");
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("person.login", "wile");
        vars.put("person.email", "wile.coyote@warnerbros.com");
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider)
            .withVariables(vars)
            .withEndConsumer(output)
            .withResourcesScriptScanner(getClass().getClassLoader(), resourceFilter);
        // When
        pipelineBuilder.scan();
        // Then
        output.assertNoScriptEndFailed();
        assertThat(output.events).isNotEmpty();
        assertThat(output.eventStream(ResultEvent.class).map(ResultEvent::getResult).collect(Collectors.toSet())).contains("0;wile;wile.coyote@warnerbros.com");
        close(connectionProvider);
    }

    @Test
    public void testConsume_Transaction() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        SingleSqlConnectionProvider connectionProvider = createFileConnectionProvider().single();
        Predicate<String> resourceFilter = name -> name.startsWith("com/github/gquintana/beepbeep/sql/init_tx/") && name.endsWith(".sql");
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider)
            .withEndConsumer(output)
            .withManualCommit()
            .withResourcesScriptScanner(getClass().getClassLoader(), resourceFilter);
        // When
        try {
            pipelineBuilder.scan();
            fail("Exception expected");
        } catch (LineException e) {
            assertThat(e.getLineEvent().getScript().getName()).isEqualTo("02_data.sql");
            assertThat(e.getLineEvent().getLineNumber()).isEqualTo(2);
        }
        // Then transaction was rollbacked
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("select count(*) from person")
        ) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(0);
        }
        close(connectionProvider);
    }

    @Test
    public void testConsume_Fail() throws Exception {
        // Given
        TestConsumer<ScriptEvent> output = new TestConsumer<>();
        SingleSqlConnectionProvider connectionProvider = createSqlConnectionProvider().single();
        Predicate<String> resourceFilter = name -> name.startsWith("com/github/gquintana/beepbeep/sql/init_fail/") && name.endsWith(".sql");
        SqlPipelineBuilder pipelineBuilder = new SqlPipelineBuilder()
            .withConnectionProvider(connectionProvider)
            .withEndConsumer(output)
            .withResourcesScriptScanner(getClass().getClassLoader(), resourceFilter);
        // When
        try {
            pipelineBuilder.scan();
            fail("Exception expected");
        } catch (BeepBeepException e) {
            // Exception expected
        }
        // Then
        assertThat(output.events).hasSize(2);
        assertThat(output.events(ScriptStartEvent.class)).hasSize(1);
        assertThat(output.events(ScriptEndEvent.class).get(0).getType()).isEqualTo(ScriptEndEvent.FAIL_TYPE);
        close(connectionProvider);
    }

    private void close(SqlConnectionProvider connectionProvider) throws Exception {
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {
            if (store) {
                dropTable(statement, "beepbeep");
            }
            dropTable(statement, "person");
            // When using sequences
            // statement.execute("DROP SEQUENCE beepbeep_seq");
        }
        connectionProvider.close();
    }

    private static void dropTable(Statement statement, String table) {
        try {
            statement.execute("DROP TABLE " + table);
        } catch (SQLException e) {

        }
    }

}
