package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.config.ConfigurationException;
import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.util.Strings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScriptScanners {
    private ScriptScanners() {
    }

    /**
     * Regular expression to analyze scheme:///path/to/file*.txt
     */
    private static Pattern AUTO_PATTERN = Pattern.compile("^(?:([a-z]+):)?(/*)(.*)$");

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
    public static CompositeScriptScanner.Builder composite() {
        return CompositeScriptScanner.builder();
    }

    private static String[] splitAutoGlob(String glob) {
        Matcher matcher = AUTO_PATTERN.matcher(glob.trim());
        if (!matcher.matches()) {
            throw new ConfigurationException("Invalid glob " + glob);
        }
        String scheme = matcher.group(1);
        if (!(scheme == null || scheme.equals("file") || scheme.equals("classpath"))) {
            throw new ConfigurationException("Invalid scheme " + scheme);
        }
        String slash = matcher.group(2);
        if (!(slash == null || slash.length() < 4)) {
            throw new ConfigurationException("Invalid slash " + glob);
        }
        String path = matcher.group(3);
        return new String[]{scheme, slash == null ? "" : Strings.left(slash, 1), path};
    }

    /**
     * Scan and use muliple scripts from class path (using classpath: scheme) or file system (using file: or no scheme)
     */
    public static ScriptScanner schemes(String glob, Consumer<ScriptStartEvent> consumer) {
        String[] parts = splitAutoGlob(glob);
        String scheme = parts[0];
        if (scheme == null || scheme.equals("file")) {
            return files(parts[1] + parts[2], consumer);
        } else if (scheme.equals("classpath")) {
            return resources(Thread.currentThread().getContextClassLoader(), parts[2], consumer);
        } else {
            throw new ConfigurationException("Invalid glob " + glob);
        }
    }

    /**
     * Use single script from class path (using classpath: scheme) or file system (using file: or no scheme)
     */
    public static ScriptScanner scheme(String script, Consumer<ScriptStartEvent> consumer) {
        String[] parts = splitAutoGlob(script);
        String scheme = parts[0];
        if (scheme == null || scheme.equals("file")) {
            return file(Paths.get(parts[1] + parts[2]), consumer);
        } else if (scheme.equals("classpath")) {
            return resource(Thread.currentThread().getContextClassLoader(), parts[2], consumer);
        } else {
            throw new ConfigurationException("Invalid glob " + script);
        }
    }
}
