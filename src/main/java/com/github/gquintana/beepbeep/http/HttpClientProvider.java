package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.util.Strings;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientProvider {
    private final String baseUri;
    public HttpClientProvider(String baseUri) {
        if (baseUri.endsWith("/")) {
            this.baseUri = Strings.left(baseUri, baseUri.length() - 1);
        } else {
            this.baseUri = baseUri;
        }
    }

    public HttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    public String getBaseUri() {
        return baseUri;
    }
}
