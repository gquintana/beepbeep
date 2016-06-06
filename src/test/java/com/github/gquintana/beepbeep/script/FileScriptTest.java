package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestFiles;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class FileScriptTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testSimple() throws IOException {
        // Given
        File folder = temporaryFolder.newFolder("sub");
        File file = new File(folder, "script.sql");
        TestFiles.writeResource("sql/init/01_create.sql", file);
        // When
        FileScript script = new FileScript(new File(temporaryFolder.getRoot(), "sub/script.sql").toPath());
        // Then
        assertThat(script.getName()).isEqualTo("script.sql");
        assertThat(script.getFullName()).isEqualTo(file.getPath());
        assertThat(script.getSize()).isEqualTo(TestFiles.getResourceSize("sql/init/01_create.sql"));
        // Scripts depend on Git crlf settings
        assertThat(script.getSize()).isIn(135L, 141L);
        assertThat(script.getSha1Hex()).isIn("b250b56d15bd419ee45ab9f5985a6bda81c7b2ea", "ebc6b90deaa80cbdf9a4719a27a214589f277164", "cf46c864faab785b2d7660f5ed17ce8eff088583");
    }

}
