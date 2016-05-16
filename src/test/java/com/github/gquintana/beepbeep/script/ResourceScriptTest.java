package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestFiles;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class ResourceScriptTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testClass() throws IOException {
        // Given
        // When
        ResourceScript script = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
        // Then
        assertThat(script.getName()).isEqualTo("01_create.sql");
        assertThat(script.getFullName()).isEqualTo("sql/init/01_create.sql");
        if (System.getProperty("os.name").equalsIgnoreCase("linux")) {
            assertThat(script.getSize()).isEqualTo(135L);
            assertThat(script.getSha1Hex()).isEqualTo("b250b56d15bd419ee45ab9f5985a6bda81c7b2ea");
        }
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
}
