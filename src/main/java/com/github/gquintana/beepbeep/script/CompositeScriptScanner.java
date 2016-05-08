package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

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

    public CompositeScriptScanner(Consumer<ScriptStartEvent> consumer) {
        super(consumer);
    }

    public CompositeScriptScanner scanner(ScriptScanner scanner) {
        scanners.add(scanner);
        return this;
    }

    /**
     * Add single script
     */
    public CompositeScriptScanner script(Script script) {
        return scanner(ScriptScanners.script(script, consumer));
    }

    /**
     * Add single script from file system
     */
    public CompositeScriptScanner file(Path file) {
        return script(new FileScript(file));
    }

    /**
     * Add multiple scripts from file system
     */
    public CompositeScriptScanner files(Path... files) {
        for (Path file : files) {
            file(file);
        }
        return this;
    }

    /**
     * Add single script from class path
     */
    public CompositeScriptScanner resource(Class clazz, String resource) {
        return script(ResourceScript.create(clazz, resource));
    }

    /**
     * Add multiple scripts from class path
     */
    public CompositeScriptScanner resources(Class clazz, String... resources) {
        for (String resource : resources) {
            resource(clazz, resource);
        }
        return this;
    }

    /**
     * Add single script from class path
     */
    public CompositeScriptScanner resource(ClassLoader classLoader, String resource) {
        return script(ResourceScript.create(classLoader, resource));
    }

    /**
     * Add multiple scripts from class path
     */
    public CompositeScriptScanner resources(ClassLoader classLoader, String... resources) {
        for (String resource : resources) {
            resource(classLoader, resource);
        }
        return this;
    }

    /**
     * Scan and add script from file system
     */
    public CompositeScriptScanner files(Path folder, Predicate<Path> fileFilter) {
        return scanner(ScriptScanners.files(folder, fileFilter, consumer));
    }

    /**
     * Scan and add script from file system, using file glob
     */
    public CompositeScriptScanner files(String fileGlob, Predicate<Path> fileFilter) {
        return scanner(ScriptScanners.files(fileGlob, consumer));
    }

    /**
     * Scan and add script from class path
     */
    public CompositeScriptScanner resources(ClassLoader classLoader, Predicate<String> resourceFilter) {
        return scanner(ScriptScanners.resources(classLoader, resourceFilter, consumer));
    }

    @Override
    public void scan() throws IOException {
        for (ScriptScanner scanner : scanners) {
            scanner.scan();
        }
    }
}
