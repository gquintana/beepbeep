package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.config.ConfigurationException;
import com.github.gquintana.beepbeep.file.FileScriptStore;
import com.github.gquintana.beepbeep.file.YamlFileScriptStore;
import com.github.gquintana.beepbeep.store.MemoryScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStoreException;
import com.github.gquintana.beepbeep.util.Uri;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ScriptStores {
    private ScriptStores() {
    }

    /**
     * Named memory script stores
     */
    private static final Map<String, MemoryScriptStore> MEMORY_SCRIPT_STORES = new HashMap<>();

    /**
     * Get named memory script stores
     */
    public static MemoryScriptStore memory(String name) {
        synchronized (MEMORY_SCRIPT_STORES) {
            MemoryScriptStore memory = MEMORY_SCRIPT_STORES.get(name);
            if (memory == null) {
                memory = new MemoryScriptStore();
                MEMORY_SCRIPT_STORES.put(name, memory);
            }
            return memory;
        }
    }

    /**
     * Get file script store
     */
    public static FileScriptStore file(Path file) {
        String fileName = file.getFileName().toString();
        if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            return new YamlFileScriptStore(file);
        } else {
            throw new ScriptStoreException("Unsupported file extension " + file);
        }
    }

    /**
     * Get memory or file script store
     */
    public static ScriptStore<Integer> scheme(String uri) {
        Uri parsedUri = Uri.valueOf(uri);
        if (parsedUri.getScheme() == null) {
            return null;
        } else if ("file".equals(parsedUri.getScheme())) {
            return file(parsedUri.toPath());
        } else if ("mem".equals(parsedUri.getScheme())) {
            return memory(uri);
        } else {
            throw new ConfigurationException("Invalid scheme" + uri);
        }
    }

}
