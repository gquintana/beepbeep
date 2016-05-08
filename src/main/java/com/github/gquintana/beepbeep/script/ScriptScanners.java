package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

import java.nio.file.Path;
import java.util.function.Predicate;

public final class ScriptScanners {
    private ScriptScanners() {
    }

    /**
     * Use single script
     */
    public static SingleScriptScanner script(Script script, Consumer<ScriptStartEvent> scriptConsumer) {
        return new SingleScriptScanner(script, scriptConsumer);
    }

    /**
     * Use single scripts from file system
     */
    public static SingleScriptScanner file(Path file, Consumer<ScriptStartEvent> scriptConsumer) {
        return script(new FileScript(file), scriptConsumer);
    }

    /**
     * Use single scripts from class path
     */
    public static SingleScriptScanner resource(Class clazz, String resource, Consumer<ScriptStartEvent> scriptConsumer) {
        return script(ResourceScript.create(clazz, resource), scriptConsumer);
    }

    /**
     * Use single scripts from class path
     */
    public static SingleScriptScanner resource(ClassLoader classLoader, String resource, Consumer<ScriptStartEvent> scriptConsumer) {
        return script(ResourceScript.create(classLoader, resource), scriptConsumer);
    }

    /**
     * Scan and use muliples scripts from file system
     */
    public static FileScriptScanner files(Path folder, Predicate<Path> fileFilter, Consumer<ScriptStartEvent> scriptConsumer) {
        return new FileScriptScanner(folder, fileFilter, scriptConsumer);
    }

    /**
     * Scan and use muliples scripts from file system using file glob syntax
     */
    public static ScriptScanner files(String fileGlob, Consumer<ScriptStartEvent> scriptConsumer) {
        return FileScriptScanner.fileGlob(fileGlob, scriptConsumer);
    }

    /**
     * Scan and use muliple scripts from class path
     */
    public static ResourceScriptScanner resources(ClassLoader classLoader, Predicate<String> resourceFilter, Consumer<ScriptStartEvent> scriptConsumer) {
        return new ResourceScriptScanner(classLoader, resourceFilter, scriptConsumer);
    }

    /**
     * Scan and use muliple scripts from class path using resource glob syntact
     */
    public static ResourceScriptScanner resources(ClassLoader classLoader, String resourceGlob, Consumer<ScriptStartEvent> scriptConsumer) {
        return ResourceScriptScanner.resourceGlob(classLoader, resourceGlob, scriptConsumer);
    }

    /**
     * Mix and use multiple scripts from file system and class path
     */
    public static CompositeScriptScanner composite(Consumer<ScriptStartEvent> scriptConsumer) {
        return new CompositeScriptScanner(scriptConsumer);
    }
}
