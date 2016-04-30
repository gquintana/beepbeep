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
        ResourceScript script = ResourceScript.create(TestFiles.class, "script/script_create.sql");
        // Then
        assertThat(script.getName()).isEqualTo("script_create.sql");
        assertThat(script.getFullName()).isEqualTo("script/script_create.sql");
        assertThat(script.getSize()).isEqualTo(135L);
    }

    @Test
    public void testClassLoader() throws IOException {
        // Given
        // When
        String resource = getClass().getPackage().getName().replaceAll("\\.", "/") + "/script_create.sql";
        ResourceScript script = ResourceScript.create(Thread.currentThread().getContextClassLoader(), resource);
        // Then
        assertThat(script.getName()).isEqualTo("script_create.sql");
        assertThat(script.getFullName()).isEqualTo(resource);
        assertThat(script.getSize()).isEqualTo(135L);
    }
}
