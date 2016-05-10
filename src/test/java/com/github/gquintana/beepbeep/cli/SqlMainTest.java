package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.http.HttpPipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.sql.DriverSqlConnectionProvider;
import com.github.gquintana.beepbeep.sql.SqlPipelineBuilder;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlMainTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testGlobal() throws Exception {
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
            assertThat(logins).contains("jdoe", "sconnor");
            statement.execute("DROP TABLE person");
        }
        dropTables(h2Url, "person");
    }

    @Test
    public void testGlobal_Store() throws Exception {
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
            "--store", "beepbeep",
            "--files", scriptFolder.getPath() + "/*.sql"};
        // When
        Main.main(args);
        // Then
        try (Connection connection = DriverSqlConnectionProvider.create(h2Url, "sa", null).getConnection();
             Statement statement = connection.createStatement()) {
            List<String> scripts = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery("SELECT full_name FROM beepbeep")) {
                while (resultSet.next()) {
                    scripts.add(resultSet.getString("full_name"));
                }
            }
            assertThat(scripts).hasSize(2);
            statement.execute("DROP TABLE person");
        }
        dropTables(h2Url, "person", "beepbeep");
    }

    public void dropTables(String h2Url, String... tables) {
        try (Connection connection = DriverSqlConnectionProvider.create(h2Url, "sa", null).getConnection();
             Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.execute("DROP TABLE " + table);
            }
        } catch (SQLException e) {

        }
    }
}
