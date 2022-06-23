package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestFiles;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyzerInputStreamTest {

    @Test
    public void testRead_Byte() throws Exception {
        // Given
        try (AnalyzerInputStream inputStream = new AnalyzerInputStream(TestFiles.getResourceAsStream("sql/init/01_create.sql"), "SHA-1")) {
            // When
            while (inputStream.read() >= 0) {
                // Read all
            }
            // Then
            assertThat(inputStream.getSize()).isIn(135L, 141L);
        }
    }

    @Test
    public void testRead_Bytes() throws Exception {
        // Given
        try (AnalyzerInputStream inputStream = new AnalyzerInputStream(TestFiles.getResourceAsStream("sql/init/01_create.sql"), "SHA-1")) {
            byte[] buffer = new byte[1000];
            // When
            while (inputStream.read(buffer) >= 0) {
                // Read all
            }
            // Then
            assertThat(inputStream.getSize()).isIn(135L, 141L);
        }
    }
}
