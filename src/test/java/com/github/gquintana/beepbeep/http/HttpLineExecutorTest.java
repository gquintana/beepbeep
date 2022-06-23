package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.LineException;
import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HttpLineExecutorTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(WireMockConfiguration.wireMockConfig().dynamicPort())
        .build();

    private BasicHttpClientProvider getBasicHttpClientProviderOnWireMock() {
        return new BasicHttpClientProvider("http://localhost:"+wireMock.getPort()+"/");
    }

    @Test
    @Ignore
    public void testGetGoogle() {
        // Given
        TestConsumer<ScriptEvent> consumer = new TestConsumer<>();
        HttpClientProvider httpClientProvider = new BasicHttpClientProvider("https://www.google.com");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer, null);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(null, 1, "GET /" + eol + "HEADER Accept text/html" + eol));
        // Then
        assertThat(consumer.events).hasSize(1);
        assertThat(consumer.events.get(0).toString()).contains("200,OK");
    }

    @Test
    public void testGet() {
        // Given
        wireMock.stubFor(get(urlMatching("/my/url")).withHeader("Accept", matching("application/json"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-type", "application/json")
                .withBody("{\"body\":\"Hello world\"}")));
        TestConsumer<ScriptEvent> consumer = new TestConsumer<>();
        HttpClientProvider httpClientProvider = getBasicHttpClientProviderOnWireMock();
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer, null);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(null, 1, "GET /my/url" + eol + "HEADER Accept application/json" + eol));
        // Then
        assertThat(consumer.events).hasSize(1);
        assertThat(consumer.events.get(0).toString()).contains("200,OK");
    }

    @Test
    public void testPost() {
        // Given
        String requestBody = "{\"body\":\"Hello world\"}";
        wireMock.stubFor(post(urlMatching("/my/url"))
            .withHeader("Accept", matching("application/json"))
            .withHeader("Content-Type", matching("application/json"))
            .withRequestBody(equalToJson(requestBody))
            .willReturn(aResponse().withStatus(201)
                .withHeader("Content-type", "application/json; charset=UTF-8")
                .withBody("{\"created\":\"true\"}")));
        TestConsumer<ScriptEvent> consumer = new TestConsumer<>();
        HttpClientProvider httpClientProvider = getBasicHttpClientProviderOnWireMock();
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer, null);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(null, 1, "POST my/url" + eol
            + "HEADER Accept application/json" + eol
            + "HEADER Content-Type application/json" + eol +
            requestBody));
        // Then
        assertThat(consumer.events).hasSize(1);
        assertThat(consumer.events.get(0).toString()).contains("201,Created,{\"created\":\"true\"}");
    }

    @Test
    public void testEmpty() {
        // Given
        TestConsumer<ScriptEvent> consumer = new TestConsumer<>();
        HttpClientProvider httpClientProvider = getBasicHttpClientProviderOnWireMock();
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer, null);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(null, 1, "# Comment" + eol
            + "" + eol
            + "\t# Comment" + eol));
        // Then
        assertThat(consumer.events).isEmpty();
    }

    @Test
    public void testParseFailure() {
        // Given
        TestConsumer<ScriptEvent> consumer = new TestConsumer<>();
        HttpClientProvider httpClientProvider = getBasicHttpClientProviderOnWireMock();
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer, null);
        // When
        assertThatThrownBy(() -> processor.consume(new LineEvent(null, 1, "FAIL /at/url")))
            .isInstanceOf(LineException.class);
        // Then
        assertThat(consumer.events).isEmpty();
    }

    @Test
    public void test404Error() {
        // Given
        TestConsumer<ScriptEvent> consumer = new TestConsumer<>();
        HttpClientProvider httpClientProvider = getBasicHttpClientProviderOnWireMock();
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer, null);
        // When
        assertThatThrownBy(() -> processor.consume(new LineEvent(null, 1, "GET /unknown/url")))
            .isInstanceOf(LineException.class);
        // Then
        assertThat(consumer.events).isEmpty();
    }

    @Test
    public void testConnectionError() {
        // Given
        TestConsumer<ScriptEvent> consumer = new TestConsumer<>();
        HttpClientProvider httpClientProvider = new BasicHttpClientProvider("http://unknown");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer, null);
        // When
        assertThatThrownBy(() -> processor.consume(new LineEvent(null, 1, "GET /unknown/url")))
            .isInstanceOf(LineException.class);
        // Then
        assertThat(consumer.events).isEmpty();
    }
}
