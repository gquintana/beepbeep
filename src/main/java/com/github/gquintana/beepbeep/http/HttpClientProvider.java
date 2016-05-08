package com.github.gquintana.beepbeep.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientProvider {
    public HttpClient getHttpClient() {
        return HttpClients.createDefault();
    }
}
