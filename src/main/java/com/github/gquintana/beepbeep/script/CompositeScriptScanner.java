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
    private final List<ScriptScannerFactory> scannerFactories;

    private CompositeScriptScanner(List<ScriptScannerFactory> scannerFactories, Consumer<ScriptStartEvent> consumer) {
        super(consumer);
        this.scannerFactories = scannerFactories;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder<B extends Builder<B>> {
        private final List<ScriptScannerFactory> scannerFactories = new ArrayList<>();

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public B scanner(ScriptScannerFactory scannerFactory) {
            scannerFactories.add(scannerFactory);
            return self();
        }

        /**
         * Add single script
         */
        public B script(Script script) {
            return scanner(ScriptScannerFactories.script(script));
        }

        /**
         * Add single script from file system
         */
        public B file(Path file) {
            return script(new FileScript(file));
        }

        /**
         * Add multiple scripts from file system
         */
        public B files(Path... files) {
            for (Path file : files) {
                file(file);
            }
            return self();
        }

        /**
         * Add single script from class path
         */
        public B resource(Class clazz, String resource) {
            return scanner(ScriptScannerFactories.resource(clazz, resource));
        }

        /**
         * Add multiple scripts from class path
         */
        public B resources(Class clazz, String... resources) {
            for (String resource : resources) {
                resource(clazz, resource);
            }
            return self();
        }

        /**
         * Add single script from class path
         */
        public B resource(ClassLoader classLoader, String resource) {
            return scanner(ScriptScannerFactories.resource(classLoader, resource));
        }

        /**
         * Add multiple scripts from class path
         */
        public B resources(ClassLoader classLoader, String... resources) {
            for (String resource : resources) {
                resource(classLoader, resource);
            }
            return self();
        }

        /**
         * Scan and add script from file system
         */
        public B files(Path folder, Predicate<Path> fileFilter) {
            return scanner(ScriptScannerFactories.files(folder, fileFilter));
        }

        /**
         * Scan and add script from file system, using file glob
         */
        public B files(String fileGlob) {
            return scanner(ScriptScannerFactories.files(fileGlob));
        }

        /**
         * Scan and add script from class path
         */
        public B resources(ClassLoader classLoader, Predicate<String> resourceFilter) {
            return scanner(ScriptScannerFactories.resources(classLoader, resourceFilter));
        }

        /**
         * Scan and use muliple scripts from class path (using classpath: scheme) or file system (using file: or no scheme)
         */
        public B schemes(String glob) {
            return scanner(ScriptScannerFactories.schemes(glob));
        }

        public CompositeScriptScanner build(Consumer<ScriptStartEvent> consumer) {
            return new CompositeScriptScanner(scannerFactories, consumer);
        }

        public ScriptScannerFactory factory() {
            switch (scannerFactories.size()) {
                case 1:
                    return scannerFactories.get(0);
                default:
                    return this::build;
            }
        }
    }

    @Override
    public void scan() throws IOException {
        for (ScriptScannerFactory scannerFactory : scannerFactories) {
            ScriptScanner scanner = scannerFactory.create(consumer);
            scanner.scan();
        }
    }
}
