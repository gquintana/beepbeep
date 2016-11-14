package com.github.gquintana.beepbeep.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gquintana.beepbeep.http.BasicHttpClientProvider;
import com.github.gquintana.beepbeep.http.HttpClientProvider;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class RemoteElasticsearchRule extends ExternalResource {
    private final RemoteElasticsearch process = new RemoteElasticsearch();
    private final String url;
    private HttpClientProvider httpClientProvider;
    private ObjectMapper objectMapper = new ObjectMapper();

    public RemoteElasticsearchRule() {
        this.url = "http://localhost:9200";
        httpClientProvider = new BasicHttpClientProvider(url);
    }

    public String getUrl() {
        return url;
    }

    public HttpClientProvider getHttpClientProvider() {
        return httpClientProvider;
    }

    public void deleteIndex(String index) throws IOException {
        HttpResponse response = httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), new HttpDelete(index + "?ignore_unavailable=true"));
        close(response);
    }

    private static void close(HttpResponse response) {
        if (response instanceof CloseableHttpResponse) {
            try {
                ((CloseableHttpResponse) response).close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void before() throws Throwable {
        process.start();
        String version = getVersion();
        if (!version.startsWith("5.")) {
            throw new IllegalStateException("Invalid Elasticsearch version " + version);
        } ;
    }

    private String getVersion() throws IOException {
        HttpResponse response = httpClientProvider.getHttpClient().execute(httpClientProvider.getHttpHost(), new HttpGet());
        try (InputStream inputStream = response.getEntity().getContent()) {
            Map map = objectMapper.readValue(inputStream, Map.class);
            return (String) ((Map) map.get("version")).get("number");
        } finally {
            close(response);
        }
    }

    @Override
    protected void after() {
        try {
            httpClientProvider.close();
        } catch (Exception e) {

        }
        process.stop();
    }
}
