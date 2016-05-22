package com.github.gquintana.beepbeep.http;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;

public interface HttpClientProvider extends AutoCloseable{
    HttpClient getHttpClient();

    HttpHost getHttpHost();

    String getBasePath();

}
