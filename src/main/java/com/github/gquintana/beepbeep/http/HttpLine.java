package com.github.gquintana.beepbeep.http;

import com.github.gquintana.beepbeep.LineException;
import com.github.gquintana.beepbeep.pipeline.LineEvent;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HttpLine {
    private final String method;
    private final String uri;
    private final List<Header> headers;
    private final String body;
    public static class Header {
        private final String name;
        private final String value;

        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    public HttpLine(String method, String uri, List<Header> headers, String body) {
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    private static Pattern METHOD_URI_PATTERN = Pattern.compile("^\\s*(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH)\\s+(.*)$");
    private static Pattern HEADER_PATTERN = Pattern.compile("^\\s*(HEADER)\\s+(\\S+)\\s+(.*)$");

    /**
     * Remove empty lines and comment lines
     */
    private static boolean isUselessLine(String line) {
        String trim = line.trim();
        return trim.isEmpty() || trim.startsWith("#");
    }

    static List<String> removeUselessLines(List<String> lines) {
        int start = 0, end = lines.size();
        for (; start < end && isUselessLine(lines.get(start)); start++) {
        }
        for (; end > start && isUselessLine(lines.get(end - 1)); end--) {
        }
        return lines.subList(start, end);
    }

    public static HttpLine parse(LineEvent lineEvent) {
        // Remove empty and comment lines at beginning and end
        List<String> subLines = new BufferedReader(new StringReader(lineEvent.getLine())).lines().collect(Collectors.toList());
        subLines = removeUselessLines(subLines);
        if (subLines.isEmpty()) {
            return null;
        }
        // Parse verb and uri
        Matcher methodUriMatcher = METHOD_URI_PATTERN.matcher(subLines.get(0));
        if (!methodUriMatcher.matches()) {
            throw new LineException("HTTP line invalid", lineEvent);
        }
        String method = methodUriMatcher.group(1);
        String uri = methodUriMatcher.group(2);
        if (uri != null) {
            uri = uri.trim();
        }
        // Parse headers
        int i = 1;
        List<HttpLine.Header> headers = new ArrayList<>();
        for (; i < subLines.size(); i++) {
            Matcher headerMatcher = HEADER_PATTERN.matcher(subLines.get(i));
            if (headerMatcher.matches()) {
                headers.add(new HttpLine.Header(headerMatcher.group(2), headerMatcher.group(3)));
            } else {
                break;
            }
        }
        // Parse body
        List<String> bodyLines = subLines.subList(i, subLines.size());
        String body = null;
        if (!bodyLines.isEmpty()) {
            String eol = System.lineSeparator();
            body = bodyLines.stream().collect(Collectors.joining(eol))+ eol;
        }
        return new HttpLine(method, uri, headers, body);
    }

}
