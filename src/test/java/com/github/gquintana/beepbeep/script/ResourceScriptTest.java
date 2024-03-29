package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestFiles;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


public class ResourceScriptTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testClass() {
        // Given
        // When
        ResourceScript script = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
        // Then
        assertThat(script.getName()).isEqualTo("01_create.sql");
        assertThat(script.getFullName()).isEqualTo("com/github/gquintana/beepbeep/sql/init/01_create.sql");
        // Scripts depend on Git crlf settings
        assertThat(script.getSize()).isIn(135L, 141L);
        assertThat(script.getSha1Hex()).isIn("b250b56d15bd419ee45ab9f5985a6bda81c7b2ea", "ebc6b90deaa80cbdf9a4719a27a214589f277164", "cf46c864faab785b2d7660f5ed17ce8eff088583");
    }

    @Test
    public void testClassLoader() throws IOException {
        // Given
        // When
        String resource = TestFiles.getResourceFullName("sql/init/01_create.sql");
        ResourceScript script = ResourceScript.create(Thread.currentThread().getContextClassLoader(), resource);
        // Then
        assertThat(script.getName()).isEqualTo("01_create.sql");
        assertThat(script.getFullName()).isEqualTo(resource);
        assertThat(script.getSize()).isEqualTo(TestFiles.getResourceSize("sql/init/01_create.sql"));
    }

    @Test
    public void testConfiguration() {
        // Given
        String resource = TestFiles.getResourceFullName("sql/init/01_create.sql");
        ResourceScript script = ResourceScript.create(Thread.currentThread().getContextClassLoader(), resource);
        // When
        script.setConfiguration("timeUnit", "SECONDS");
        // Then
        assertThat(script.getConfiguration("timeUnit", String.class).get()).isEqualTo("SECONDS");
        assertThat(script.getConfiguration("timeUnit", TimeUnit.class).get()).isEqualTo(TimeUnit.SECONDS);
        assertThat(script.getConfiguration("unknown", String.class).isPresent()).isFalse();
    }
}
