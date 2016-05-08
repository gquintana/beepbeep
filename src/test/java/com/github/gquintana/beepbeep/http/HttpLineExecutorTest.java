package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.LineException;
import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpLineExecutorTest {
    @Rule
    public WireMockRule wireMock = new WireMockRule(WireMockConfiguration.wireMockConfig().port(8080));

    @Test
    @Ignore
    public void testGetGoogle() {
        // Given
        TestConsumer consumer = new TestConsumer();
        HttpClientProvider httpClientProvider = new HttpClientProvider("http://www.google.com");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(1, "GET /" + eol + "HEADER Accept text/html" + eol));
        // Then
        assertThat(consumer.events).hasSize(1);
        assertThat(consumer.events.get(0).toString()).startsWith("200,OK");
    }

    @Test
    public void testGet() {
        // Given
        wireMock.stubFor(get(urlMatching("/my/url")).withHeader("Accept", matching("application/json"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-type", "application/json")
                .withBody("{\"body\":\"Hello world\"}")));
        TestConsumer consumer = new TestConsumer();
        HttpClientProvider httpClientProvider = new HttpClientProvider("http://localhost:8080/");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(1, "GET /my/url" + eol + "HEADER Accept application/json" + eol));
        // Then
        assertThat(consumer.events).hasSize(1);
        assertThat(consumer.events.get(0).toString()).startsWith("200,OK");
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
        TestConsumer consumer = new TestConsumer();
        HttpClientProvider httpClientProvider = new HttpClientProvider("http://localhost:8080/");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(1, "POST my/url" + eol
            + "HEADER Accept application/json" + eol
            + "HEADER Content-Type application/json" + eol +
            requestBody));
        // Then
        assertThat(consumer.events).hasSize(1);
        assertThat(consumer.events.get(0).toString()).startsWith("201,Created,{\"created\":\"true\"}");
    }

    @Test
    public void testEmpty() {
        // Given
        TestConsumer consumer = new TestConsumer();
        HttpClientProvider httpClientProvider = new HttpClientProvider("http://localhost:8080/");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(1, "# Comment" + eol
            + "" + eol
            + "\t# Comment" + eol));
        // Then
        assertThat(consumer.events).isEmpty();
    }

    @Test(expected = LineException.class)
    public void testParseFailure() {
        // Given
        TestConsumer consumer = new TestConsumer();
        HttpClientProvider httpClientProvider = new HttpClientProvider("http://localhost:8080/");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(1, "FAIL /at/url"));
        // Then
        assertThat(consumer.events).isEmpty();
    }

    @Test(expected = LineException.class)
    public void test404Error() {
        // Given
        TestConsumer consumer = new TestConsumer();
        HttpClientProvider httpClientProvider = new HttpClientProvider("http://localhost:8080/");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(1, "GET /unknown/url"));
        // Then
        assertThat(consumer.events).isEmpty();
    }

    @Test(expected = LineException.class)
    public void testConnectionError() {
        // Given
        TestConsumer consumer = new TestConsumer();
        HttpClientProvider httpClientProvider = new HttpClientProvider("http://unknown");
        HttpLineExecutor processor = new HttpLineExecutor(httpClientProvider, consumer);
        // When
        String eol = System.lineSeparator();
        processor.consume(new LineEvent(1, "GET /unknown/url"));
        // Then
        assertThat(consumer.events).isEmpty();
    }
}
