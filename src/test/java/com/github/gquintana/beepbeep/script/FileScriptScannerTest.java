package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class FileScriptScannerTest {
    @TempDir
    Path tempDir;

    @Test
    public void testScanFolder() throws IOException {
        // Given
        Path createFile = copySqlFiles(tempDir);
        TestConsumer<ScriptStartEvent> consumer = new TestConsumer<>();
        FileScriptScanner scanner = new FileScriptScanner(tempDir,
            path -> path.toString().endsWith(".sql"), consumer);
        // When
        scanner.scan();
        List<FileScript> scripts = consumer.scriptStream(FileScript.class)
            .collect(Collectors.toList());
        // Then
        assertThat(scripts).hasSize(3);
        assertThat(scripts.get(0).getName()).isEqualTo("create.sql");
        assertThat(scripts.get(0).getFullName()).isEqualTo(createFile.toString());
        assertThat(scripts.get(0).getSize()).isEqualTo(TestFiles.getResourceSize("sql/init/01_create.sql"));
        assertThat(scripts.get(1).getName()).isEqualTo("data.sql");
        assertThat(scripts.get(2).getName()).isEqualTo("drop.sql");
    }

    private Path copySqlFiles(Path targetFolder) throws IOException {
        Path createFolder = targetFolder.resolve( "create");
        Files.createDirectories(createFolder);
        Path dropFolder = targetFolder.resolve( "drop");
        Files.createDirectories(dropFolder);
        Path createFile = createFolder.resolve("create.sql");
        TestFiles.writeResource("sql/init/01_create.sql", createFile);
        TestFiles.writeResource("sql/init/02_data.sql", createFolder.resolve( "data.sql"));
        TestFiles.writeResource("sql/clean/drop.sql", dropFolder.resolve( "drop.sql"));
        return createFile;
    }

}
