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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Map;

import static com.github.gquintana.beepbeep.TestReflect.getField;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationLoaderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


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
        assertThat(getField(pipelineBuilder, "charset")).isEqualTo(Charset.forName("UTF-8"));
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
    public void testSql1Yml_Resource() throws Exception {
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
        File configurationFile = new File(temporaryFolder.getRoot(), "sql.yml");
        TestFiles.writeResource("config/sql1.yml", configurationFile);
        // When
        PipelineBuilder pipelineBuilder = configurationLoader.loadFile(configurationFile.toPath());
        // Then
        assertThat(pipelineBuilder).isInstanceOf(SqlPipelineBuilder.class);
    }

    @Test
    public void testSqlSnakeCaseYml_File() throws Exception {
        // Given
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        File configurationFile = new File(temporaryFolder.getRoot(), "sql.yml");
        TestFiles.writeResource("config/sql_snakecase.yml", configurationFile);
        // When
        PipelineBuilder pipelineBuilder = configurationLoader.loadFile(configurationFile.toPath());
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
        File configurationFile = new File(temporaryFolder.getRoot(), "sql.yml");
        TestFiles.writeResource("config/sql_scriptstore.yml", configurationFile);
        // When
        PipelineBuilder pipelineBuilder = configurationLoader.loadFile(configurationFile.toPath());
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
        assertThat(getField(pipelineBuilder, "charset")).isEqualTo(Charset.forName("ISO-8859-1"));
        assertThat(getField(pipelineBuilder, "url")).isEqualTo("http://localhost:9200");
        Object scriptStore = getField(pipelineBuilder, "scriptStore");
        assertThat(scriptStore).isInstanceOf(ElasticsearchScriptStore.class);
        assertThat(getField(scriptStore, "indexType")).isEqualTo("beepbeep/script");
        assertThat(scriptScanner).isInstanceOf(FileScriptScanner.class);
        Map variables = (Map) getField(pipelineBuilder, "variables");
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
        assertThat(getField(pipelineBuilder, "charset")).isEqualTo(Charset.forName("UTF-8"));
    }

    @Test(expected = ConfigurationException.class)
    public void testInvalid1Yml() throws Exception {
        // Given
        // When
        PipelineBuilder pipelineBuilder = loadPipeline("config/invalid1.yml");
        // Then
    }

    @Test(expected = ConfigurationException.class)
    public void testInvalid2Yml() throws Exception {
        // Given
        // When
        PipelineBuilder pipelineBuilder = loadPipeline("config/invalid2.yml");
        // Then
    }
}
