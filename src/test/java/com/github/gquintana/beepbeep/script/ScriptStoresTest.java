package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.file.YamlFileScriptStore;
import com.github.gquintana.beepbeep.store.MemoryScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptStoresTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

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
    public void testFile() throws IOException {
        // Given
        File yamlFile = temporaryFolder.newFile("store.yml");
        // When
        ScriptStore store = ScriptStores.scheme(yamlFile.toURI().toString());
        // Then
        assertThat(store).isInstanceOf(YamlFileScriptStore.class);
    }


}
