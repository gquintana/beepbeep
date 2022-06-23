package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.TestElasticsearch;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.http.BasicHttpClientProvider;
import com.github.gquintana.beepbeep.http.HttpClientProvider;
import org.apache.http.client.methods.HttpDelete;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class ElasticsearchMainTest {
    @TempDir
    Path tempDir;

    @Container
    ElasticsearchContainer elasticsearchContainer = TestElasticsearch.createContainer();

    protected void writeScripts(Path scriptFolder) throws IOException {
        TestFiles.writeResource("elasticsearch/index_create.json", scriptFolder.resolve("01_create.json"));
        TestFiles.writeResource("elasticsearch/index_data.json", scriptFolder.resolve("02_data.json"));
    }

    protected String getEsUrl() {
        return "http://" + elasticsearchContainer.getHttpHostAddress();
    }

    @Test
    public void testGlobal() throws Exception {
        // Given
        Path scriptFolder = tempDir.resolve("script");
        Files.createDirectories(scriptFolder);
        writeScripts(scriptFolder);
        String esUrl = getEsUrl();
        String[] args = {
            "--type", "elasticsearch",
            "--url", esUrl,
            "--files", scriptFolder.toAbsolutePath() + "/*.json"};
        // When
        int exit = Main.doMain(args);
        // Then
        assertThat(exit).isEqualTo(0);
        deleteIndices(esUrl, "person");
    }

    @Test
    public void testGlobal_Store() throws Exception {
        // Given
        Path scriptFolder = tempDir.resolve("script");
        Files.createDirectories(scriptFolder);
        writeScripts(scriptFolder);
        String esUrl = getEsUrl();
        String[] args = {
            "--type", "elasticsearch",
            "--url", esUrl,
            "--username", "sa",
            "--store", "beepbeep",
            "--files", scriptFolder.toAbsolutePath() + "/*.json"};
        // When
        int exit = Main.doMain(args);
        // Then
        assertThat(exit).isEqualTo(0);
        deleteIndices(esUrl, "person", "beepbeep");
    }

    public void deleteIndices(String esUrl, String... indices) {
        try {
            HttpClientProvider httpClientProvider = new BasicHttpClientProvider(esUrl);
            HttpDelete httpRequest = new HttpDelete(String.join(",", indices) + "?ignore_unavailable=true");
            httpRequest.setHeader("Accept", "application/json");
            httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), httpRequest);
        } catch (IOException e) {
        }
    }
}
