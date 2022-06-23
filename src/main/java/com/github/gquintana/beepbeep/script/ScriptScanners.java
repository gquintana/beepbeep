package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.config.ConfigurationException;
import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.util.Uri;

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
     * Scan and use multiples scripts from file system
     */
    public static FileScriptScanner files(Path folder, Predicate<Path> fileFilter, Consumer<ScriptStartEvent> scriptConsumer) {
        return new FileScriptScanner(folder, fileFilter, scriptConsumer);
    }

    /**
     * Scan and use multiples scripts from file system using file glob syntax
     */
    public static ScriptScanner files(String fileGlob, Consumer<ScriptStartEvent> scriptConsumer) {
        return FileScriptScanner.fileGlob(fileGlob, scriptConsumer);
    }

    /**
     * Scan and use multiple scripts from class path
     */
    public static ResourceScriptScanner resources(ClassLoader classLoader, Predicate<String> resourceFilter, Consumer<ScriptStartEvent> scriptConsumer) {
        return new ResourceScriptScanner(classLoader, resourceFilter, scriptConsumer);
    }

    /**
     * Scan and use multiple scripts from class path using resource glob syntact
     */
    public static ResourceScriptScanner resources(ClassLoader classLoader, String resourceGlob, Consumer<ScriptStartEvent> scriptConsumer) {
        return ResourceScriptScanner.resourceGlob(classLoader, resourceGlob, scriptConsumer);
    }

    /**
     * Mix and use multiple scripts from file system and class path
     */
    public static CompositeScriptScanner.Builder composite() {
        return CompositeScriptScanner.builder();
    }

    private static Uri parseUri(String glob) {
        Uri uri = Uri.valueOf(glob);
        String scheme = uri.getScheme();
        if (!(scheme == null || scheme.equals("file") || scheme.equals("classpath"))) {
            throw new ConfigurationException("Invalid scheme " + scheme);
        }
        return uri;
    }

    /**
     * Scan and use multiple scripts from class path (using classpath: scheme) or file system (using file: or no scheme)
     */
    public static ScriptScanner schemes(String glob, Consumer<ScriptStartEvent> consumer) {
        Uri uri = parseUri(glob);
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            return files(uri.getPath(), consumer);
        } else if (scheme.equals("classpath")) {
            return resources(Thread.currentThread().getContextClassLoader(), uri.getPath(), consumer);
        } else {
            throw new ConfigurationException("Invalid glob " + glob);
        }
    }

    /**
     * Use single script from class path (using classpath: scheme) or file system (using file: or no scheme)
     */
    public static ScriptScanner scheme(String script, Consumer<ScriptStartEvent> consumer) {
        Uri uri = parseUri(script);
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            return file(uri.toPath(), consumer);
        } else if (scheme.equals("classpath")) {
            return resource(Thread.currentThread().getContextClassLoader(), uri.getPath(), consumer);
        } else {
            throw new ConfigurationException("Invalid glob " + script);
        }
    }
}
