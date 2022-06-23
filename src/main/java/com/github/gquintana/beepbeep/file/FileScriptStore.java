package com.github.gquintana.beepbeep.file;

import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStoreException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Store ran script in a file
 */
public abstract class FileScriptStore implements ScriptStore<Integer> {
    private final Path file;
    private int nextId = 1;

    public FileScriptStore(Path file) {
        this.file = file;
    }

    protected abstract Stream<ScriptInfo<Integer>> load() throws  IOException;

    protected abstract void save(List<ScriptInfo<Integer>> infos) throws IOException;

    @Override
    public void prepare() {
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            throw new ScriptStoreException("Store file prepare failed", e);
        }
    }

    @Override
    public ScriptInfo<Integer> getByFullName(String fullName) {
        try {
            return load()
                .filter(i -> i.getFullName().equals(fullName))
                .findFirst().orElse(null);
        } catch (IOException e) {
            throw new ScriptStoreException("Store file load failed", e);
        }
    }

    @Override
    public ScriptInfo<Integer> create(ScriptInfo<Integer> info) {
        try {
            List<ScriptInfo<Integer>> infos = load().collect(Collectors.toCollection(ArrayList::new));
            info.setId(nextId++);
            infos.add(info);
            save(infos);
            return info;
        } catch (IOException e) {
            throw new ScriptStoreException("Store file save failed", e);
        }
    }

    private ScriptInfo<Integer> update(ScriptInfo<Integer> before, ScriptInfo<Integer> after) {
        if (!before.getId().equals(after.getId())) {
            return before;
        } else if (Objects.equals(before.getVersionAsInt(), after.getVersionAsInt())) {
            after.setVersion(after.getVersionAsInt() + 1);
            return after;
        } else {
            throw new ScriptStoreException("Concurrent modification of script " + before.getFullName());
        }
    }

    @Override
    public ScriptInfo<Integer> update(ScriptInfo<Integer> info) {
        try {
            int versionBefore = Integer.parseInt(info.getVersion());
            List<ScriptInfo<Integer>> infos = load()
                .map(i -> update(i, info))
                .collect(Collectors.toList());
            if (versionBefore + 1 != Integer.parseInt(info.getVersion())) {
                throw new ScriptStoreException("Can not update script " + info.getFullName());
            }
            save(infos);
            return info;
        } catch (IOException e) {
            throw new ScriptStoreException("Store file save failed", e);
        }
    }

    public Path getFile() {
        return file;
    }
}
