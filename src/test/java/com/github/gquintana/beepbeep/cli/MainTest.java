package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.elasticsearch.ElasticsearchPipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.sql.SqlPipelineBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;

import static com.github.gquintana.beepbeep.TestReflect.getField;
import static org.assertj.core.api.Assertions.assertThat;

public class MainTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testInvalidArgument() throws Exception {
        // Given
        // When
        int exit = Main.doMain("--invalid",
            "--type", "sql",
            "--url", "jdbc:h2:mem:test",
            "--username", "sa",
            "--files", "target/test-classes/com/github/gquintana/beepbeep/**/*.sql");
        // Then
        assertThat(exit).isEqualTo(1);
    }

    @Test
    public void testInvalidType() throws Exception {
        // Given
        // When
        int exit = Main.doMain(
            "--type", "invalid",
            "--url", "jdbc:h2:mem:test",
            "--username", "sa",
            "--files", "target/test-classes/com/github/gquintana/beepbeep/**/*.sql");
        // Then
        assertThat(exit).isEqualTo(2);
    }

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
        PipelineBuilder pipelineBuilder = main.createPipelineBuilder();
        // Then
        assertThat(main.url).isEqualTo("jdbc:h2:mem:test");
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);

    }

    @Test
    public void testCreatePipelineBuilder_SqlConfiguration() throws Exception {
        // Given
        File configFile = new File(temporaryFolder.getRoot(), "sql.yml");
        TestFiles.writeResource("config/sql1.yml", configFile);
        Main main = new Main();
        CmdLineParser cmdLineParser = new CmdLineParser(main);
        cmdLineParser.parseArgument(
            "--config", configFile.getPath());
        // When
        PipelineBuilder pipelineBuilder = main.createPipelineBuilder();
        // Then
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);
        assertThat(getField(pipelineBuilder, "url")).isEqualTo("jdbc:h2:mem:test");

    }

    @Test
    public void testCreatePipelineBuilder_Elasticsearch() throws Exception {
        // Given
        Main main = new Main();
        CmdLineParser cmdLineParser = new CmdLineParser(main);
        cmdLineParser.parseArgument(
            "--type", "elasticsearch",
            "--url", "http://localhost:9200",
            "--files", "target/test-classes/com/github/gquintana/beepbeep/**/*.json");
        // When
        PipelineBuilder pipelineBuilder = main.createPipelineBuilder();
        // Then
        assertThat(main.url).isEqualTo("http://localhost:9200");
        assertThat(pipelineBuilder).isInstanceOf(ElasticsearchPipelineBuilder.class);
    }

}
