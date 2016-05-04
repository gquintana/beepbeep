package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.LineException;
import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.gquintana.beepbeep.pipeline.LineExecutor;
import com.github.gquintana.beepbeep.util.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class HttpLineExecutor extends LineExecutor {
    private final HttpClientProvider httpClientProvider;
    private HttpClient httpClient;


    public HttpLineExecutor(HttpClientProvider httpClientProvider, Consumer consumer) {
        super(consumer);
        this.httpClientProvider = httpClientProvider;
    }

    @Override
    protected void executeStart() {
        if (httpClient == null) {
            httpClient = httpClientProvider.getHttpClient();
        }
    }

    private HttpRequestBase createHttpRequest(HttpLine httpLine) throws UnsupportedEncodingException {
        HttpRequestBase request;
        // Verb / URI
        String uri = httpLine.getUri();
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        uri = httpClientProvider.getBaseUri() + uri;
        switch (httpLine.getMethod()) {
            case "GET":
                request = new HttpGet(uri);
                break;
            case "PUT":
                request = new HttpPut(uri);
                break;
            case "POST":
                request = new HttpPost(uri);
                break;
            case "DELETE":
                request = new HttpDelete(uri);
                break;
            case "HEAD":
                request = new HttpHead(uri);
                break;
            case "OPTIONS":
                request = new HttpOptions(uri);
                break;
            case "PATCH":
                request = new HttpPatch(uri);
                break;
            default:
                request = null;
        }
        // Headers
        String contentType = null;
        if (httpLine.getHeaders() != null) {
            for (HttpLine.Header header : httpLine.getHeaders()) {
                request.addHeader(header.getName(), header.getValue());
                if (header.getName().equals("Content-Type")) {
                    contentType = header.getValue();
                }
            }
        }
        // Body / Entity
        if (Strings.isNotNullNorEmpty(httpLine.getBody()) && request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase httpEntityRequest = (HttpEntityEnclosingRequestBase) request;
            ContentType httpContentType = ContentType.parse(contentType);
            StringEntity entity = contentType == null ? new StringEntity(httpLine.getBody()) : new StringEntity(httpLine.getBody(), httpContentType);
            httpEntityRequest.setEntity(entity);
        }
        return request;
    }

    @Override
    protected void executeLine(LineEvent lineEvent) {
        executeStart();
        try {
            HttpLine httpLine = HttpLine.parse(lineEvent);
            if (httpLine == null) {
                return;
            }
            HttpRequestBase httpRequest = createHttpRequest(httpLine);
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode >= 400 && statusCode < 599) {
                throw new LineException("HTTP Status " + statusCode + " " + httpResponse.getStatusLine().getReasonPhrase(), lineEvent);
            }
            produce(httpResponse);
        } catch (IOException e) {
            throw new LineException("HTTP I/O failure", e, lineEvent);
        }
    }

    private void produce(HttpResponse httpResponse) throws IOException {
        String resultEvent = httpResponse.getStatusLine().getStatusCode() + "," + httpResponse.getStatusLine().getReasonPhrase();
        if (httpResponse.getEntity() != null) {
            ContentType contentType = ContentType.get(httpResponse.getEntity());
            Charset charset = contentType.getCharset() == null ? Charset.forName("UTF-8") : contentType.getCharset();
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                httpResponse.getEntity().writeTo(byteArrayOutputStream);
                resultEvent += "," + new String(byteArrayOutputStream.toByteArray(), charset);
            }
        }
        produce(resultEvent);
    }

}
