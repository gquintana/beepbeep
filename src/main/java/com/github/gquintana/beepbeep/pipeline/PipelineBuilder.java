package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.script.*;
import com.github.gquintana.beepbeep.store.ScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStoreFilter;
import com.github.gquintana.beepbeep.store.ScriptStoreUpdater;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class PipelineBuilder<B extends PipelineBuilder<B>> {
    protected Charset charset = Charset.forName("UTF-8");
    protected String url;
    protected String username;
    protected String password;
    protected ScriptStore scriptStore;
    private Map<String, Object> variables;
    protected Consumer<ScriptEvent> endConsumer = event -> {
    };
    private ScriptScannerFactory scriptScannerFactory;

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    public B withEndConsumer(Consumer<ScriptEvent> endConsumer) {
        this.endConsumer = endConsumer;
        return self();
    }

    public B withCharset(Charset charset) {
        this.charset = charset;
        return self();
    }

    public B withVariables(Map<String, Object> variables) {
        if (this.variables == null) {
            this.variables = new HashMap<>(variables);
        } else {
            this.variables.putAll(variables);
        }
        return self();
    }

    public B withVariable(String name, Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>();
        }
        this.variables.put(name, value);
        return self();
    }

    public B withUrl(String url) {
        this.url = url;
        return self();
    }

    public B withUsername(String username) {
        this.username = username;
        return self();
    }

    public B withPassword(String password) {
        this.password = password;
        return self();
    }

    public ScriptStore getScriptStore() {
        return scriptStore;
    }

    public B withScriptStore(ScriptStore scriptStore) {
        this.scriptStore = scriptStore;
        return self();
    }

    public abstract B withScriptStore(String name);

    protected Consumer<ScriptEvent> notNullNorEmptyFilter(Consumer<ScriptEvent> consumer) {
        return LineFilter.<ScriptEvent>notNulNotEmptyFilter(consumer);
    }

    protected Consumer<ScriptEvent> variableReplacer(Consumer<ScriptEvent> consumer) {
        if (variables != null && !variables.isEmpty()) {
            consumer = new VariableReplacerProcessor(variables, consumer);
        }
        return consumer;
    }

    protected Consumer<ScriptStartEvent> scriptReader(Consumer<ScriptEvent> consumer) {
        if (scriptStore != null) {
            consumer = new ScriptStoreUpdater(scriptStore, consumer);
        }
        Consumer<ScriptStartEvent> startConsumer = new ScriptReaderProducer(consumer, charset);
        if (scriptStore != null) {
            startConsumer = new ScriptStoreFilter(scriptStore, startConsumer);
        }
        return startConsumer;
    }

    /**
     * Create consumer chain
     * @return Head of consumer chain
     */
    public abstract Consumer<ScriptStartEvent> createConsumers();


    /**
     * Create consumer chain and scanner
     */
    public ScriptScanner createScriptScanner() {
        if (scriptScannerFactory == null) {
            throw new BeepBeepException("No script scanner configured");
        }
        Consumer<ScriptStartEvent> consumer = createConsumers();
        return scriptScannerFactory.create(consumer);
    }

    /**
     * Create consumer chain and scanner, then run scanner
     */
    public void scan() throws IOException {
        // Prepare script store if needed
        if (scriptStore != null) {
            scriptStore.prepare();
        }
        // Run script scanner
        ScriptScanner scriptScanner = createScriptScanner();
        scriptScanner.scan();
    }

    @FunctionalInterface
    private interface ScriptScannerFactory {
        ScriptScanner create(Consumer<ScriptStartEvent> consumer);
    }

    private B withScriptScanner(ScriptScannerFactory scriptScriptScannerFactory) {
        this.scriptScannerFactory = scriptScriptScannerFactory;
        return self();
    }

    /**
     * Use single script
     */
    public B withScriptScanner(Script script) {
        return withScriptScanner(c -> ScriptScanners.script(script, c));
    }

    /**
     * Use single scripts from file system
     */
    public B withFileScriptScanner(Path file) {
        return withScriptScanner(c -> ScriptScanners.file(file, c));
    }

    /**
     * Use single scripts from class path
     */
    public B withResourceScriptScanner(Class clazz, String resource) {
        return withScriptScanner(x -> ScriptScanners.resource(clazz, resource, x));
    }

    /**
     * Use single scripts from class path
     */
    public B withResourceScriptScanner(ClassLoader classLoader, String resource) {
        return withScriptScanner(x -> ScriptScanners.resource(classLoader, resource, x));
    }

    /**
     * Scan and use muliples scripts from file system
     */
    public B withFilesScriptScanner(Path folder, Predicate<Path> fileFilter) {
        return withScriptScanner(x -> ScriptScanners.files(folder, fileFilter, x));
    }

    /**
     * Scan and use muliples scripts from file system using file glob syntax
     */
    public B withFilesScriptScanner(String fileGlob) {
        return withScriptScanner(x -> ScriptScanners.files(fileGlob, x));
    }

    /**
     * Scan and use muliple scripts from class path
     */
    public B withResourcesScriptScanner(ClassLoader classLoader, Predicate<String> resourceFilter) {
        return withScriptScanner(x -> ScriptScanners.resources(classLoader, resourceFilter, x));
    }

    /**
     * Scan and use muliple scripts from class path using resource glob syntact
     */
    public B withResourcesScriptScanner(ClassLoader classLoader, String resourceGlob) {
        return withScriptScanner(x -> ScriptScanners.resources(classLoader, resourceGlob, x));
    }

}
