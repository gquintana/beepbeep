package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.elasticsearch.ElasticsearchRule;
import com.github.gquintana.beepbeep.http.HttpClientProvider;
import com.github.gquintana.beepbeep.sql.DriverSqlConnectionProvider;
import org.apache.http.client.methods.HttpDelete;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticsearchMainTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final ElasticsearchRule elasticsearchRule = new ElasticsearchRule(temporaryFolder);

    protected void writeScripts(File scriptFolder) throws IOException {
        TestFiles.writeResource("elasticsearch/index_create.json", new File(scriptFolder, "01_create.json"));
        TestFiles.writeResource("elasticsearch/index_data.json", new File(scriptFolder, "02_data.json"));
    }

    protected String getEsUrl() {
        return "http://" + elasticsearchRule.getElasticsearch().getHttpAddress();
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
        Main.main(args);
        // Then
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
        Main.main(args);
        // Then
        deleteIndices(esUrl, "person", "beepbeep");
    }

    public void deleteIndices(String esUrl, String... indices) {
        try {
            HttpClientProvider httpClientProvider = new HttpClientProvider(esUrl);
            HttpDelete httpRequest = new HttpDelete(Arrays.stream(indices).collect(Collectors.joining(",")) + "?ignore_unavailable=true");
            httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), httpRequest);
        } catch (IOException e) {
        }
    }
}
