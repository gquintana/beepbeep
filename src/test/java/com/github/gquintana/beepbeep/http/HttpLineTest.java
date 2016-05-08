package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.pipeline.LineEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class HttpLineTest {
    @Test
    public void testRemoveUselessLines() {
        // Given
        String eol = System.lineSeparator();
        List<String> lines1 = asList(
            "#Comment" + eol,
            "" + eol,
            "  # Comment" + eol,
            "POST /url" + eol,
            "{" + eol,
            "\"body\":\"content\"" + eol,
            "}" + eol,
            "" + eol,
            " # Comment" + eol
        );
        // When
        List<String> lines = HttpLine.removeUselessLines(lines1);
        // Then
        assertThat(lines).hasSize(4);
    }

    @Test
    public void testRemoveUselessLines_Empty() {
        // Given
        String eol = System.lineSeparator();
        List<String> lines1 = asList(
            "#Comment" + eol,
            "" + eol,
            "  # Comment" + eol,
            "" + eol,
            " # Comment" + eol
        );
        // When
        List<String> lines = HttpLine.removeUselessLines(lines1);
        // Then
        assertThat(lines).isEmpty();
    }

    @Test
    public void testParse() throws Exception {
        // Given

    }

    @Test
    public void testParsePostBody() throws UnsupportedEncodingException {
        // Given
        String eol = System.lineSeparator();
        LineEvent lineEvent = new LineEvent(null, 10,
            "#Comment" + eol +
                "" + eol +
                "  # Comment" + eol +
                "POST /url" + eol +
                "{" + eol +
                "\"body\":\"content\"" + eol +
                "}" + eol +
                "" + eol +
                " # Comment" + eol
        );
        // When
        HttpLine httpLine = HttpLine.parse(lineEvent);
        // Then
        assertThat(httpLine.getMethod()).isEqualTo("POST");
        assertThat(httpLine.getUri()).isEqualTo("/url");
        assertThat(httpLine.getBody()).isEqualTo("{" + eol +
            "\"body\":\"content\"" + eol +
            "}" + eol);
        assertThat(httpLine.getHeaders()).isEmpty();
    }

    @Test
    public void testParseGetHeaders() throws UnsupportedEncodingException {
        // Given
        String eol = System.lineSeparator();
        LineEvent lineEvent = new LineEvent(null, 10,
            "#Comment" + eol +
                "" + eol +
                "  # Comment" + eol +
                "GET /my/endpoint?is=beautiful" + eol +
                "HEADER Accept application/json" + eol +
                "# Comment" + eol
        );
        // When
        HttpLine httpLine = HttpLine.parse(lineEvent);
        // Then
        assertThat(httpLine.getMethod()).isEqualTo("GET");
        assertThat(httpLine.getUri()).isEqualTo("/my/endpoint?is=beautiful");
        assertThat(httpLine.getHeaders()).hasSize(1);
    }

    @Test
    public void testParseEmpty() throws UnsupportedEncodingException {
        // Given
        String eol = System.lineSeparator();
        LineEvent lineEvent = new LineEvent(null, 10,
            "#Comment" + eol +
                "" + eol +
                "  # Comment" + eol
        );
        // When
        HttpLine httpLine = HttpLine.parse(lineEvent);
        // Then
        assertThat(httpLine).isNull();
    }
}
