package com.github.gquintana.beepbeep.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class UriTest {
    private final String input;
    private final String scheme;
    private final String path;

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        String userDir = System.getProperty("user.dir").replace(File.separatorChar, '/');
        return new ParametersBuilder()
            .add("file:///path/to/file.txt", "file", "/path/to/file.txt")
            .add("file:/path/to/file.txt", "file", "/path/to/file.txt")
            .add("file:path/to/file.txt", "file", "path/to/file.txt")
            .add("classpath:/path/to/file.txt", "classpath", "/path/to/file.txt")
            .add("classpath:path/to/file.txt", "classpath", "path/to/file.txt")
            .add("/path/to/file.txt", null, "/path/to/file.txt")
            .add("path/to/file.txt", null, "path/to/file.txt")
            .add(Paths.get(userDir).toUri().toString(), "file", (windows ? "/" : "") + userDir + "/")
            .build();
    }

    public UriTest(String input, String scheme, String path) {
        this.input = input;
        this.scheme = scheme;
        this.path = path;
    }

    @Test
    public void testValueOf() throws Exception {
        // Given
        // When
        Uri uri = Uri.valueOf(input);
        // Then
        if (scheme == null) {
            assertThat(uri.getScheme()).isNull();
        } else {
            assertThat(uri.getScheme()).isEqualTo(scheme);
        }
        assertThat(uri.getPath()).isEqualTo(path);
    }
}
