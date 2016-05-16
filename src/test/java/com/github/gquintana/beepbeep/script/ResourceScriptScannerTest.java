package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class ResourceScriptScannerTest {

    @Test
    public void testScanClassLoader() throws IOException {
        // Given
        ClassLoader classLoader = getClass().getClassLoader();
        TestConsumer<ScriptStartEvent> consumer = new TestConsumer<>();
        ResourceScriptScanner scanner = new ResourceScriptScanner(classLoader, name -> name.startsWith("com/github/gquintana/beepbeep/sql/init/") && name.endsWith(".sql"), consumer);
        // When
        scanner.scan();
        List<ResourceScript> scripts = consumer.scriptStream(ResourceScript.class).collect(Collectors.toList());
        // Then
        assertThat(scripts).hasSize(2);
        assertThat(scripts.get(0).getName()).isEqualTo("01_create.sql");
        assertThat(scripts.get(1).getName()).isEqualTo("02_data.sql");
    }
}
