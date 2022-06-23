package com.github.gquintana.beepbeep.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class UriTest {
    public static Stream<Arguments> getParameters() {
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


    @ParameterizedTest
    @MethodSource("getParameters")
    public void testValueOf(String input, String scheme, String path) {
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
