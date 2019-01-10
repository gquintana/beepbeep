package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.LineException;
import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.gquintana.beepbeep.pipeline.LineExecutor;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.util.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HttpLineExecutor extends LineExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLineExecutor.class);
    private final HttpClientProvider httpClientProvider;
    private final List<HttpHeader> defaultHttpHeaders;
    private HttpClient httpClient;


    public HttpLineExecutor(HttpClientProvider httpClientProvider, Consumer<ScriptEvent> consumer, List<HttpHeader> defaultHttpHeaders) {
        super(consumer);
        this.httpClientProvider = httpClientProvider;
        this.defaultHttpHeaders = defaultHttpHeaders;
    }

    @Override
    protected void executeStart() {
        if (httpClient == null) {
            httpClient = httpClientProvider.getHttpClient();
        }
    }

    @Override
    protected void executeEnd(boolean success) {
        if (httpClient instanceof CloseableHttpClient) {
            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException e) {
                throw new BeepBeepException("Failed to close HTTP client", e);
            }
        }
        httpClient = null;
    }

    private static final class HttpHeaderAccumulator {
        private final List<HttpHeader> list = new ArrayList<>();
        private final Set<String> nameSet = new HashSet<>();
        private String contentType;
        private HttpHeaderAccumulator addAll(Iterable<HttpHeader> headers) {
            if (headers == null) {
                return this;
            }
            for (HttpHeader header: headers) {
                if (nameSet.add(header.getName())) {
                    list.add(header);
                    if (header.getName().equals("Content-Type")) {
                        contentType = header.getValue();
                    }
                }
            }
            return this;
        }
    }

    private HttpRequestBase createHttpRequest(HttpLine httpLine) throws UnsupportedEncodingException {
        HttpRequestBase request;
        // Verb / URI
        String uri = httpLine.getUri();
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        uri = httpClientProvider.getBasePath() + uri;
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
                throw new BeepBeepException("Unsupported HTTP method " + httpLine.getMethod());
        }
        // Headers
        HttpHeaderAccumulator headerAccu = new HttpHeaderAccumulator().addAll(httpLine.getHeaders()).addAll(defaultHttpHeaders);
        String contentType = headerAccu.contentType;
        for (HttpHeader header : headerAccu.list) {
            request.addHeader(header.getName(), header.getValue());
        }
        // Body / Entity
        if (Strings.isNotNullNorEmpty(httpLine.getBody()) && request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase httpEntityRequest = (HttpEntityEnclosingRequestBase) request;
            StringEntity entity;
            if (contentType == null) {
                entity = new StringEntity(httpLine.getBody());
            } else {
                ContentType httpContentType = ContentType.parse(contentType);
                entity = new StringEntity(httpLine.getBody(), httpContentType);
            }
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
            HttpResponse httpResponse = httpClient.execute(httpClientProvider.getHttpHost(), httpRequest);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode >= 400 && statusCode < 599) {
                if (LOGGER.isDebugEnabled()) {
                    try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        httpResponse.getEntity().writeTo(outputStream);
                        LOGGER.debug("HTTP Response {} {}: {}", statusCode,httpResponse.getStatusLine().getReasonPhrase(), new String(outputStream.toByteArray(),new String(outputStream.toByteArray())));
                    } catch (IOException e) {
                        LOGGER.debug("HTTP Response {} {}: {}", statusCode,httpResponse.getStatusLine().getReasonPhrase(), e.getMessage());
                    }
                }
                throw new LineException("HTTP Status " + statusCode + " " + httpResponse.getStatusLine().getReasonPhrase(), lineEvent);
            }
            produce(lineEvent, httpResponse);
        } catch (IOException e) {
            throw new LineException("HTTP I/O failure", e, lineEvent);
        }
    }

    private void produce(LineEvent lineEvent, HttpResponse httpResponse) throws IOException {
        String resultEvent = httpResponse.getStatusLine().getStatusCode() + "," + httpResponse.getStatusLine().getReasonPhrase();
        if (httpResponse.getEntity() != null) {
            ContentType contentType = ContentType.get(httpResponse.getEntity());
            Charset charset = contentType.getCharset() == null ? Charset.forName("UTF-8") : contentType.getCharset();
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                httpResponse.getEntity().writeTo(byteArrayOutputStream);
                resultEvent += "," + new String(byteArrayOutputStream.toByteArray(), charset);
            }
        }
        produce(lineEvent, resultEvent);
    }

}
