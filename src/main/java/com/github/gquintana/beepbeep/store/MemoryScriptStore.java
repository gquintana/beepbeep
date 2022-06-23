package com.github.gquintana.beepbeep.store;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link ScriptStore} base a simple in memory HashMap.
 * Mostly for testing purpose.
 */
public class MemoryScriptStore implements ScriptStore<Integer> {
    private final Map<Integer, ScriptInfo<Integer>> byId = new HashMap<>();
    private final Map<String, ScriptInfo<Integer>> byFullName = new HashMap<>();
    private int nextId = 1;

    @Override
    public void prepare() {

    }

    @Override
    public ScriptInfo<Integer> getByFullName(String fullName) {
        ScriptInfo<Integer> original = byFullName.get(fullName);
        if (original == null) {
            return null;
        }
        ScriptInfo<Integer> copy = new ScriptInfo<>();
        copy(original, copy);
        return copy;
    }

    @Override
    public ScriptInfo<Integer> create(ScriptInfo<Integer> info) {
        synchronized (this) {
            ScriptInfo<Integer> original = byFullName.get(info.getFullName());
            if (original != null) {
                throw new ScriptStoreException("Script already exists  " + info.getFullName());
            }
            original = new ScriptInfo<>();
            copy(info, original);
            original.setId(nextId++);
            original.setVersion(1);
            byFullName.put(original.getFullName(), original);
            byId.put(original.getId(), original);
            info.setId(original.getId());
            info.setVersion(original.getVersion());
            return info;
        }
    }

    @Override
    public ScriptInfo<Integer> update(ScriptInfo<Integer> info) {
        synchronized (this) {
            ScriptInfo<Integer> original = byId.get(info.getId());
            if (original == null) {
                throw new ScriptStoreException("Script not found " + info.getFullName());
            }
            if (!original.getVersion().equals(info.getVersion())) {
                throw new ScriptStoreException("Concurrent modification of script " + info.getFullName());
            }
            copy(info, original);
            int version = original.getVersion() == null ? 0 : original.getVersionAsInt();
            version++;
            original.setVersion(version);
            info.setVersion(version);
            return info;
        }
    }

    private void copy(ScriptInfo<Integer> source, ScriptInfo<Integer> dest) {
        dest.setId(source.getId());
        dest.setVersion(source.getVersion());
        dest.setFullName(source.getFullName());
        dest.setStatus(source.getStatus());
        dest.setSize(source.getSize());
        dest.setSha1(source.getSha1());
        dest.setStartDate(source.getStartDate());
        dest.setEndDate(source.getEndDate());
    }
}
