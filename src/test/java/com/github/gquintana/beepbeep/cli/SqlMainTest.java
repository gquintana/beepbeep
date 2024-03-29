package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.sql.DriverSqlConnectionProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.github.gquintana.beepbeep.sql.TestSqlConnectionProviders.createSqlConnectionProvider;
import static org.assertj.core.api.Assertions.assertThat;

public class SqlMainTest {
    @TempDir
    Path tempDir;

    @Test
    public void testGlobal() throws Exception {
        // Given
        Path scriptFolder = tempDir.resolve("sql");
        Files.createDirectories(scriptFolder);
        Path h2Folder = tempDir.resolve("h2");
        TestFiles.writeResource("sql/init/01_create.sql", scriptFolder.resolve( "01_create.sql"));
        TestFiles.writeResource("sql/init/02_data.sql", scriptFolder.resolve( "02_data.sql"));
        String h2Url = "jdbc:h2:file:" + h2Folder.toAbsolutePath();
        String[] args = {
            "--type", "sql",
            "--url", h2Url,
            "--username", "sa",
            "--files", scriptFolder.toAbsolutePath() + "/*.sql"};
        // When
        int exit = Main.doMain(args);
        // Then
        assertThat(exit).isEqualTo(0);
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
        dropTables("person");
    }

    @Test
    public void testGlobal_Store() throws Exception {
        // Given
        Path scriptFolder = tempDir.resolve("sql");
        Files.createDirectories(scriptFolder);
        Path h2Folder = tempDir.resolve("h2");
        TestFiles.writeResource("sql/init/01_create.sql", scriptFolder.resolve( "01_create.sql"));
        TestFiles.writeResource("sql/init/02_data.sql", scriptFolder.resolve( "02_data.sql"));
        String h2Url = "jdbc:h2:file:" + h2Folder.toAbsolutePath();
        String[] args = {
            "--type", "sql",
            "--url", h2Url,
            "--username", "sa",
            "--store", "beepbeep",
            "--files", scriptFolder.toAbsolutePath() + "/*.sql"};
        // When
        int exit = Main.doMain(args);
        // Then
        assertThat(exit).isEqualTo(0);
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
        dropTables("person", "beepbeep");
    }

    public void dropTables(String... tables) {
        try (Connection connection = createSqlConnectionProvider().getConnection();
             Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.execute("DROP TABLE " + table);
            }
        } catch (SQLException e) {

        }
    }
}
