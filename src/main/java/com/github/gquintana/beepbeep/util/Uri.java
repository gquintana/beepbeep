package com.github.gquintana.beepbeep.util;

import com.github.gquintana.beepbeep.config.ConfigurationException;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Uri {
    /**
     * Regular expression to analyze scheme:///path/to/file*.txt
     */
    private static Pattern SCHEME_PATTERN = Pattern.compile("^(?:([a-z]+):)?(/*)(.*)$");
    private final String scheme;
    private final String path;

    public Uri(String scheme, String path) {
        this.scheme = scheme;
        this.path = path;
    }

    public String getScheme() {
        return scheme;
    }

    public String getPath() {
        return path;
    }

    public static Uri valueOf(String uri) {
        Matcher matcher = SCHEME_PATTERN.matcher(uri.trim());
        if (!matcher.matches()) {
            throw new ConfigurationException("Invalid URI " + uri);
        }
        String scheme = matcher.group(1);
        String slash = matcher.group(2);
        if (slash == null || slash.length() == 0 || slash.length() == 2) {
            slash = ""; // Relative path
        } else if (slash.length() == 1 || slash.length() == 3) {
            slash = "/"; // Absolute path
        } else {
            throw new ConfigurationException("Invalid slash " + uri);
        }
        String path = matcher.group(3);
        return new Uri(scheme, slash + path);
    }

    public URI toURI() throws URISyntaxException {
        return new URI(scheme + "://" + path);
    }

    public Path toPath() {
        try {
            return Paths.get(toURI());
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Invalid path " + path);
        }
    }
}
