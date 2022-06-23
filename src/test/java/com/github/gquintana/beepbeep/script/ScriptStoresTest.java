package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.file.YamlFileScriptStore;
import com.github.gquintana.beepbeep.store.MemoryScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptStoresTest {
    @TempDir
    Path tempDir;

    @Test
    public void testMemory() {
        // When
        ScriptStore store1 = ScriptStores.scheme("mem:test");
        ScriptStore store2 = ScriptStores.scheme("mem:test");
        ScriptStore store3 = ScriptStores.scheme("mem:other");
        // Then
        assertThat(store1).isInstanceOf(MemoryScriptStore.class);
        assertThat(store1).isEqualTo(store2);
        assertThat(store1).isNotEqualTo(store3);
    }

    @Test
    public void testFile() {
        // Given
        Path yamlFile = tempDir.resolve("store.yml");
        // When
        ScriptStore store = ScriptStores.scheme(yamlFile.toUri().toString());
        // Then
        assertThat(store).isInstanceOf(YamlFileScriptStore.class);
    }


}
