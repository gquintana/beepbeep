package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.http.HttpPipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.sql.DriverSqlConnectionProvider;
import com.github.gquintana.beepbeep.sql.SqlPipelineBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

public class MainTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testCreatePipelineBuilder_Sql() throws Exception {
        // Given
        Main main = new Main();
        CmdLineParser cmdLineParser = new CmdLineParser(main);
        cmdLineParser.parseArgument(
            "--type", "sql",
            "--url", "jdbc:h2:mem:test",
            "--username", "sa",
            "--files", "target/test-classes/com/github/gquintana/beepbeep/**/*.sql");
        // When
        PipelineBuilder pipelineBuilder = main.createPipelineBuilder(cmdLineParser);
        // Then
        assertThat(main.url).isEqualTo("jdbc:h2:mem:test");
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);

    }

    @Test
    public void testCreatePipelineBuilder_Http() throws Exception {
        // Given
        Main main = new Main();
        CmdLineParser cmdLineParser = new CmdLineParser(main);
        cmdLineParser.parseArgument(
            "--type", "http",
            "--url", "http://localhost:8080/restapp/",
            "--files", "target/test-classes/com/github/gquintana/beepbeep/**/*.json");
        // When
        PipelineBuilder pipelineBuilder = main.createPipelineBuilder(cmdLineParser);
        // Then
        assertThat(main.url).isEqualTo("http://localhost:8080/restapp/");
        assertThat(pipelineBuilder).isInstanceOf(HttpPipelineBuilder.class);
    }

    @Test
    public void testGlobal_Sql() throws Exception {
        // Given
        File scriptFolder = temporaryFolder.newFolder("sql");
        File h2Folder = temporaryFolder.newFolder("h2");
        TestFiles.writeResource("script/script_create.sql", new File(scriptFolder, "01_create.sql"));
        TestFiles.writeResource("script/script_data.sql", new File(scriptFolder, "02_data.sql"));
        String h2Url = "jdbc:h2:file:" + h2Folder.getPath();
        String[] args = {
            "--type", "sql",
            "--url", h2Url,
            "--username", "sa",
            "--files", scriptFolder.getPath() + "/*.sql"};
        // When
        Main.main(args);
        // Then
        try (Connection connection = DriverSqlConnectionProvider.create(h2Url, "sa", null).getConnection();
             Statement statement = connection.createStatement()) {
            List<String> logins = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM person")) {
                while (resultSet.next()) {
                    logins.add(resultSet.getString("login"));
                }
            }
            assertThat(logins).hasSize(2);
            assertThat(logins).contains("jdoe","sconnor");
            statement.execute("DROP TABLE person");
        }

    }
}
