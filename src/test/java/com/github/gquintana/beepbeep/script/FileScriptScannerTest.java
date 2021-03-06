package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class FileScriptScannerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testScanFolder() throws IOException {
        // Given
        File createFile = copySqlFiles(temporaryFolder.getRoot());
        TestConsumer<ScriptStartEvent> consumer = new TestConsumer<>();
        FileScriptScanner scanner = new FileScriptScanner(temporaryFolder.getRoot().toPath(),
            path -> path.toString().endsWith(".sql"), consumer);
        // When
        scanner.scan();
        List<FileScript> scripts = consumer.scriptStream(FileScript.class)
            .collect(Collectors.toList());
        // Then
        assertThat(scripts).hasSize(3);
        assertThat(scripts.get(0).getName()).isEqualTo("create.sql");
        assertThat(scripts.get(0).getFullName()).isEqualTo(createFile.getPath());
        assertThat(scripts.get(0).getSize()).isEqualTo(TestFiles.getResourceSize("sql/init/01_create.sql"));
        assertThat(scripts.get(1).getName()).isEqualTo("data.sql");
        assertThat(scripts.get(2).getName()).isEqualTo("drop.sql");
    }

    private File copySqlFiles(File targetFolder) throws IOException {
        File createFolder = new File(targetFolder, "create");
        createFolder.mkdirs();
        File dropFolder = new File(targetFolder, "drop");
        dropFolder.mkdirs();
        File createFile = new File(createFolder, "create.sql");
        TestFiles.writeResource("sql/init/01_create.sql", createFile);
        TestFiles.writeResource("sql/init/02_data.sql", new File(createFolder, "data.sql"));
        TestFiles.writeResource("sql/clean/drop.sql", new File(dropFolder, "drop.sql"));
        return createFile;
    }

}
