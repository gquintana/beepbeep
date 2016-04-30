package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestFiles;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class FileScriptTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testSimple() throws IOException {
        // Given
        File folder = temporaryFolder.newFolder("sub");
        File file = new File(folder, "script.sql");
        TestFiles.writeResource("script/script_create.sql", file);
        // When
        FileScript script = new FileScript(new File(temporaryFolder.getRoot(), "sub/script.sql").toPath());
        // Then
        assertThat(script.getName()).isEqualTo("script.sql");
        assertThat(script.getFullName()).isEqualTo(file.getPath());
        assertThat(script.getSize()).isEqualTo(135L);
    }

    @Test
    public void testScanFolder() throws IOException {
        // Given
        File createFolder = temporaryFolder.newFolder("create");
        File dropFolder = temporaryFolder.newFolder("drop");
        File createFile = new File(createFolder, "create.sql");
        TestFiles.writeResource("script/script_create.sql", createFile);
        TestFiles.writeResource("script/script_data.sql", new File(createFolder, "data.sql"));
        TestFiles.writeResource("script/script_drop.sql", new File(dropFolder, "drop.sql"));
        // When
        List<FileScript> scripts = FileScript.scanFolder(temporaryFolder.getRoot().toPath(), file -> file.toString().endsWith(".sql"));
        // Then
        assertThat(scripts).hasSize(3);
        assertThat(scripts.get(0).getName()).isEqualTo("create.sql");
        assertThat(scripts.get(0).getFullName()).isEqualTo(createFile.getPath());
        assertThat(scripts.get(0).getSize()).isEqualTo(135L);
        assertThat(scripts.get(1).getName()).isEqualTo("data.sql");
        assertThat(scripts.get(2).getName()).isEqualTo("drop.sql");
    }
}
