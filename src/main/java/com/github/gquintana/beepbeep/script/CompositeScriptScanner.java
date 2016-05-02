package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Execute several scripts or several file globs
 */
public class CompositeScriptScanner extends ScriptScanner {
    private final List<ScriptScanner> scanners = new ArrayList<>();

    public CompositeScriptScanner(Consumer<Script> scriptConsumer) {
        super(scriptConsumer);
    }

    public CompositeScriptScanner add(ScriptScanner scanner) {
        scanners.add(scanner);
        return this;
    }

    /**
     * Add single script
     */
    public CompositeScriptScanner add(Script script) {
        return add(new SingleScriptScanner(script, scriptConsumer));
    }

    /**
     * Add single script from file system
     */
    public CompositeScriptScanner add(Path file) {
        return add(new FileScript(file));
    }

    /**
     * Add multiple scripts from file system
     */
    public CompositeScriptScanner add(Path... files) {
        for (Path file : files) {
            add(file);
        }
        return this;
    }

    /**
     * Add single script from class path
     */
    public CompositeScriptScanner add(Class clazz, String resource) {
        return add(ResourceScript.create(clazz, resource));
    }

    /**
     * Add multiple scripts from class path
     */
    public CompositeScriptScanner add(Class clazz, String... resources) {
        for (String resource : resources) {
            add(clazz, resource);
        }
        return this;
    }

    /**
     * Add single script from class path
     */
    public CompositeScriptScanner add(ClassLoader classLoader, String resource) {
        return add(ResourceScript.create(classLoader, resource));
    }

    /**
     * Add multiple scripts from class path
     */
    public CompositeScriptScanner add(ClassLoader classLoader, String... resources) {
        for (String resource : resources) {
            add(classLoader, resource);
        }
        return this;
    }

    /**
     * Scan and add script from file system
     */
    public CompositeScriptScanner add(Path folder, Predicate<Path> fileFilter) {
        return add(new FileScriptScanner(folder, fileFilter, scriptConsumer));
    }

    /**
     * Scan and add script from class path
     */
    public CompositeScriptScanner add(ClassLoader classLoader, Predicate<String> resourceFilter) {
        return add(new ResourceScriptScanner(classLoader, resourceFilter, scriptConsumer));
    }

    @Override
    public void scan() throws IOException {
        for (ScriptScanner scanner : scanners) {
            scanner.scan();
        }
    }
}
