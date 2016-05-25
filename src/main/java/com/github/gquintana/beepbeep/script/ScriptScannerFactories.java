package com.github.gquintana.beepbeep.script;

import java.nio.file.Path;
import java.util.function.Predicate;

public final class ScriptScannerFactories {
    /**
     * Use single script
     */
    public static ScriptScannerFactory<ScriptScanner> script(Script script) {
        return c -> ScriptScanners.script(script, c);
    }

    /**
     * Use single scripts from file system
     */
    public static ScriptScannerFactory<SingleScriptScanner> file(Path file) {
        return c -> ScriptScanners.file(file, c);
    }

    /**
     * Use single scripts from class path
     */
    public static ScriptScannerFactory<SingleScriptScanner> resource(Class clazz, String resource) {
        return x -> ScriptScanners.resource(clazz, resource, x);
    }

    /**
     * Use single scripts from class path
     */
    public static ScriptScannerFactory<SingleScriptScanner> resource(ClassLoader classLoader, String resource) {
        return x -> ScriptScanners.resource(classLoader, resource, x);
    }

    /**
     * Scan and use muliples scripts from file system
     */
    public static ScriptScannerFactory<FileScriptScanner> files(Path folder, Predicate<Path> fileFilter) {
        return x -> ScriptScanners.files(folder, fileFilter, x);
    }

    /**
     * Scan and use muliples scripts from file system using file glob syntax
     */
    public static ScriptScannerFactory<ScriptScanner> files(String fileGlob) {
        return x -> ScriptScanners.files(fileGlob, x);
    }

    /**
     * Scan and use muliple scripts from class path
     */
    public static ScriptScannerFactory<ResourceScriptScanner> resources(ClassLoader classLoader, Predicate<String> resourceFilter) {
        return x -> ScriptScanners.resources(classLoader, resourceFilter, x);
    }

    /**
     * Scan and use muliple scripts from class path using resource glob syntact
     */
    public static ScriptScannerFactory<ScriptScanner> resources(ClassLoader classLoader, String resourceGlob) {
        return x -> ScriptScanners.resources(classLoader, resourceGlob, x);
    }
    /**
     * Scan and use muliple scripts from class path (using classpath: scheme) or file system (using file: or no scheme)
     */
    public static ScriptScannerFactory<ScriptScanner> schemes(String glob) {
        return x -> ScriptScanners.schemes(glob, x);
    }

    /**
     * Use single script from class path (using classpath: scheme) or file system (using file: or no scheme)
     */
    public static ScriptScannerFactory<ScriptScanner> scheme(String glob) {
        return x -> ScriptScanners.scheme(glob, x);
    }
}
