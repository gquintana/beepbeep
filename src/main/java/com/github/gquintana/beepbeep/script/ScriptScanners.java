package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

import java.nio.file.Path;
import java.util.function.Predicate;

public final class ScriptScanners {
    private ScriptScanners() {
    }

    /**
     * Use single script
     */
    public static SingleScriptScanner script(Script script, Consumer<Script> scriptConsumer) {
        return new SingleScriptScanner(script, scriptConsumer);
    }

    /**
     * Use single scripts from file system
     */
    public static SingleScriptScanner file(Path file, Consumer<Script> scriptConsumer) {
        return script(new FileScript(file), scriptConsumer);
    }

    /**
     * Use single scripts from class path
     */
    public static SingleScriptScanner resource(Class clazz, String resource, Consumer<Script> scriptConsumer) {
        return script(ResourceScript.create(clazz, resource), scriptConsumer);
    }

    /**
     * Use single scripts from class path
     */
    public static SingleScriptScanner resource(ClassLoader classLoader, String resource, Consumer<Script> scriptConsumer) {
        return script(ResourceScript.create(classLoader, resource), scriptConsumer);
    }

    /**
     * Scan and use muliples scripts from file system
     */
    public static FileScriptScanner files(Path folder, Predicate<Path> fileFilter, Consumer<Script> scriptConsumer) {
        return new FileScriptScanner(folder, fileFilter, scriptConsumer);
    }

    /**
     * Scan and use muliple scripts from class path
     */
    public static ResourceScriptScanner resources(ClassLoader classLoader, Predicate<String> resourceFilter, Consumer<Script> scriptConsumer) {
        return new ResourceScriptScanner(classLoader, resourceFilter, scriptConsumer);
    }

    /**
     * Mix and use multiple scripts from file system and class path
     */
    public static CompositeScriptScanner composite(Consumer<Script> scriptConsumer) {
        return new CompositeScriptScanner(scriptConsumer);
    }
}
