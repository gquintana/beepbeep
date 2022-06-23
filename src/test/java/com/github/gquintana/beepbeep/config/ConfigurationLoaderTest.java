package com.github.gquintana.beepbeep.config;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.elasticsearch.ElasticsearchPipelineBuilder;
import com.github.gquintana.beepbeep.elasticsearch.ElasticsearchScriptStore;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.script.CompositeScriptScanner;
import com.github.gquintana.beepbeep.script.FileScriptScanner;
import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.script.ScriptScanner;
import com.github.gquintana.beepbeep.sql.SqlPipelineBuilder;
import com.github.gquintana.beepbeep.sql.SqlScriptStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static com.github.gquintana.beepbeep.TestReflect.getField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConfigurationLoaderTest {
    @TempDir
    Path tempDir;


    /**
     * Load config file from class path
     */
    private PipelineBuilder loadPipeline(String resource) throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader();
        try (InputStream inputStream = TestFiles.getResourceAsStream(resource)) {
            return loader.load(inputStream);
        }
    }

    @Test
    public void testSql1Yml() throws Exception {
        // Given
        // When
        PipelineBuilder pipelineBuilder = loadPipeline("config/sql1.yml");
        ScriptScanner scriptScanner = pipelineBuilder.createScriptScanner();
        // Then
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);
        assertThat(getField(pipelineBuilder, "charset")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(getField(pipelineBuilder, "url")).isEqualTo("jdbc:h2:mem:test");
        assertThat(getField(pipelineBuilder, "username")).isEqualTo("sa");
        assertThat(getField(pipelineBuilder, "password")).isEqualTo(null);
        assertThat(getField(pipelineBuilder, "autoCommit")).isEqualTo(false);
        Object scriptStore = getField(pipelineBuilder, "scriptStore");
        assertThat(scriptStore).isInstanceOf(SqlScriptStore.class);
        assertThat(getField(scriptStore, "table")).isEqualTo("beepbeep");
        assertThat(scriptScanner).isInstanceOf(CompositeScriptScanner.class);
    }

    @Test
    public void testSql1Yml_Resource() {
        // Given
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        // When
        PipelineBuilder pipelineBuilder = configurationLoader.loadResource(ResourceScript.getResourceFullName(TestFiles.class, "config/sql1.yml"));
        // Then
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);
    }

    @Test
    public void testSql1Yml_File() throws Exception {
        // Given
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        Path configurationFile = tempDir.resolve("sql.yml");
        TestFiles.writeResource("config/sql1.yml", configurationFile);
        // When
        PipelineBuilder pipelineBuilder = configurationLoader.loadFile(configurationFile);
        // Then
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);
    }

    @Test
    public void testSqlSnakeCaseYml_File() throws Exception {
        // Given
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        Path configurationFile = tempDir.resolve( "sql.yml");
        TestFiles.writeResource("config/sql_snakecase.yml", configurationFile);
        // When
        PipelineBuilder pipelineBuilder = configurationLoader.loadFile(configurationFile);
        // Then
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);
        SqlScriptStore scriptStore = (SqlScriptStore) getField(pipelineBuilder, "scriptStore");
        assertThat(getField(scriptStore, "table")).isEqualTo("snake");
        assertThat(getField(pipelineBuilder, "autoCommit")).isEqualTo(false);
    }

    @Test
    public void testSqlScripStoreYml_File() throws Exception {
        // Given
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        Path configurationFile = tempDir.resolve( "sql.yml");
        TestFiles.writeResource("config/sql_scriptstore.yml", configurationFile);
        // When
        PipelineBuilder pipelineBuilder = configurationLoader.loadFile(configurationFile);
        // Then
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);
        assertThat(getField(pipelineBuilder, "scriptStoreReRunChanged")).isEqualTo(true);
        assertThat(getField(pipelineBuilder, "scriptStoreReRunFailed")).isEqualTo(false);
        assertThat(getField(pipelineBuilder, "scriptStoreReRunStartedTimeout")).isEqualTo(Duration.ofMinutes(5L));
    }

    @Test
    public void testSql2Yml() throws Exception {
        // Given
        // When
        PipelineBuilder pipelineBuilder = loadPipeline("config/sql2.yml");
        ScriptScanner scriptScanner = pipelineBuilder.createScriptScanner();
        // Then
        assertThat(getField(pipelineBuilder, "endOfLineRegex")).isEqualTo("\\Q//\\E\\s*$");
    }

    @Test
    public void testElasticsearchYml() throws Exception {
        // Given
        // When
        PipelineBuilder pipelineBuilder = loadPipeline("config/elasticsearch.yml");
        ScriptScanner scriptScanner = pipelineBuilder.createScriptScanner();
        // Then
        assertThat(pipelineBuilder).isInstanceOf(ElasticsearchPipelineBuilder.class);
        assertThat(getField(pipelineBuilder, "charset")).isEqualTo(StandardCharsets.ISO_8859_1);
        assertThat(getField(pipelineBuilder, "url")).isEqualTo("http://localhost:9200");
        Object scriptStore = getField(pipelineBuilder, "scriptStore");
        assertThat(scriptStore).isInstanceOf(ElasticsearchScriptStore.class);
        assertThat(getField(scriptStore, "index")).isEqualTo("beepbeep");
        assertThat(scriptScanner).isInstanceOf(FileScriptScanner.class);
        Map<String, Object> variables = (Map<String, Object>) getField(pipelineBuilder, "variables");
        assertThat(variables).hasSize(2);
        assertThat(variables.get("test.1")).isEqualTo("Test 1");
        assertThat(variables.get("test.2")).isEqualTo(2);

    }

    @Test
    public void testCustomYml() throws Exception {
        // Given
        // When
        PipelineBuilder pipelineBuilder = loadPipeline("config/custom.yml");
        ScriptScanner scriptScanner = pipelineBuilder.createScriptScanner();
        // Then
        assertThat(pipelineBuilder).isInstanceOf(TestPipelineBuilder.class);
        assertThat(getField(pipelineBuilder, "charset")).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    public void testInvalid1Yml() {
        // Given
        // When
        assertThatThrownBy(() ->loadPipeline("config/invalid1.yml"))
            .isInstanceOf(ConfigurationException.class);
        // Then
    }

    @Test
    public void testInvalid2Yml() {
        // Given
        // When
        assertThatThrownBy(() -> loadPipeline("config/invalid2.yml"))
            .isInstanceOf(ConfigurationException.class);
        // Then
    }
}
