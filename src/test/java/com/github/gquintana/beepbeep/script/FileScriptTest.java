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

}
