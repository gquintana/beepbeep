package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.elasticsearch.ElasticsearchRule;
import com.github.gquintana.beepbeep.http.BasicHttpClientProvider;
import com.github.gquintana.beepbeep.http.HttpClientProvider;
import org.apache.http.client.methods.HttpDelete;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticsearchMainTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final ElasticsearchRule elasticsearchRule = new ElasticsearchRule();

    protected void writeScripts(File scriptFolder) throws IOException {
        TestFiles.writeResource("elasticsearch/index_create.json", new File(scriptFolder, "01_create.json"));
        TestFiles.writeResource("elasticsearch/index_data.json", new File(scriptFolder, "02_data.json"));
    }

    protected String getEsUrl() {
        return elasticsearchRule.getHttpHostAddress();
    }

    @Test
    public void testGlobal() throws Exception {
        // Given
        File scriptFolder = temporaryFolder.newFolder("script");
        writeScripts(scriptFolder);
        String esUrl = getEsUrl();
        String[] args = {
            "--type", "elasticsearch",
            "--url", esUrl,
            "--files", scriptFolder.getPath() + "/*.json"};
        // When
        int exit = Main.doMain(args);
        // Then
        assertThat(exit).isEqualTo(0);
        deleteIndices(esUrl, "person");
    }

    @Test
    public void testGlobal_Store() throws Exception {
        // Given
        File scriptFolder = temporaryFolder.newFolder("script");
        writeScripts(scriptFolder);
        String esUrl = getEsUrl();
        String[] args = {
            "--type", "elasticsearch",
            "--url", esUrl,
            "--username", "sa",
            "--store", "beepbeep/script",
            "--files", scriptFolder.getPath() + "/*.json"};
        // When
        int exit = Main.doMain(args);
        // Then
        assertThat(exit).isEqualTo(0);
        deleteIndices(esUrl, "person", "beepbeep");
    }

    public void deleteIndices(String esUrl, String... indices) {
        try {
            HttpClientProvider httpClientProvider = new BasicHttpClientProvider(esUrl);
            HttpDelete httpRequest = new HttpDelete(Arrays.stream(indices).collect(Collectors.joining(",")) + "?ignore_unavailable=true");
            httpRequest.setHeader("Accept", "application/json");
            httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), httpRequest);
        } catch (IOException e) {
        }
    }
}
