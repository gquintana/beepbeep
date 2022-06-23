package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


public class FileScriptTest {
    @TempDir
    Path tempDir;

    @Test
    public void testSimple() throws IOException {
        // Given
        Path folder = tempDir.resolve("sub");
        Files.createDirectories(folder);
        Path file = folder.resolve( "script.sql");
        TestFiles.writeResource("sql/init/01_create.sql", file);
        // When
        FileScript script = new FileScript(tempDir.resolve( "sub/script.sql"));
        // Then
        assertThat(script.getName()).isEqualTo("script.sql");
        assertThat(script.getFullName()).isEqualTo(file.toString());
        assertThat(script.getSize()).isEqualTo(TestFiles.getResourceSize("sql/init/01_create.sql"));
        // Scripts depend on Git crlf settings
        assertThat(script.getSize()).isIn(135L, 141L);
        assertThat(script.getSha1Hex()).isIn("b250b56d15bd419ee45ab9f5985a6bda81c7b2ea", "ebc6b90deaa80cbdf9a4719a27a214589f277164", "cf46c864faab785b2d7660f5ed17ce8eff088583");
    }

}
