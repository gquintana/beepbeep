package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.util.Strings;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class BasicHttpClientProvider implements HttpClientProvider {
    private String url;
    private String basePath;
    private String username;
    private String password;
    private HttpHost httpHost;


    public BasicHttpClientProvider() {
    }

    public BasicHttpClientProvider(String url) {
        setUrl(url);
    }

    protected CredentialsProvider getCredentialsProvider() {
        if (Strings.isNullOrEmpty(username)) {
            return null;
        }
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }

    @Override
    public CloseableHttpClient getHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CredentialsProvider credentialsProvider = getCredentialsProvider();
        if (credentialsProvider != null) {
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        return httpClientBuilder.build();
    }

    @Override
    public HttpHost getHttpHost() {
        return httpHost;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        try {
            URL url1 = new URL(url);
            String scheme = url1.getProtocol();
            String host = url1.getHost();
            int port = url1.getPort() < 0 ? url1.getDefaultPort() : url1.getPort();
            basePath = url1.getPath();
            if (basePath.endsWith("/")) {
                this.basePath = Strings.left(basePath, basePath.length() - 1);
            }
            httpHost = new HttpHost(host, port, scheme);
            this.url = url;
        } catch (MalformedURLException e) {
            throw new BeepBeepException("Invalid HTTP URL " + url, e);
        }
    }

    @Override
    public void close() throws IOException {

    }

}
