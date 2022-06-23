package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.config.ConfigurationException;
import com.github.gquintana.beepbeep.script.CompositeScriptScanner;
import com.github.gquintana.beepbeep.script.Script;
import com.github.gquintana.beepbeep.script.ScriptScanner;
import com.github.gquintana.beepbeep.script.ScriptScannerFactories;
import com.github.gquintana.beepbeep.script.ScriptScannerFactory;
import com.github.gquintana.beepbeep.store.ScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStoreFilter;
import com.github.gquintana.beepbeep.store.ScriptStoreUpdater;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class PipelineBuilder<B extends PipelineBuilder<B>> {
    protected Charset charset = StandardCharsets.UTF_8;
    protected String url;
    protected String username;
    protected String password;
    protected ScriptStore scriptStore;
    /**
     * Re-run script when it is modified
     */
    private Boolean scriptStoreReRunChanged;
    /**
     * Re-run script when it previously failed
     */
    private Boolean scriptStoreReRunFailed;
    /**
     * Re-run script when it's stuck in started state after given timeout
     */
    private TemporalAmount scriptStoreReRunStartedTimeout;
    private Boolean scriptReaderIgnoreErrors;
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

    /**
     * Re-run script when it is modified
     */
    public B withScriptStoreReRunChanged(boolean reRunChanged) {
        this.scriptStoreReRunChanged = reRunChanged;
        return self();
    }
    /**
     * Re-run script when it previously failed
     */
    public B withScriptStoreReRunFailed(boolean reRunFailed) {
        this.scriptStoreReRunFailed = reRunFailed;
        return self();
    }
    /**
     * Re-run script when it's stuck in started state after given timeout
     */
    public B withScriptStoreReRunStartedTimeout(TemporalAmount reRunStartedTimeout) {
        this.scriptStoreReRunStartedTimeout = reRunStartedTimeout;
        return self();
    }

    /**
     * Continue executing scripts when an error occurs
     */
    public B withScriptReaderIgnoreErrors(Boolean ignoreErrors) {
        this.scriptReaderIgnoreErrors = ignoreErrors;
        return self();
    }

    protected Consumer<ScriptEvent> notNullNorEmptyFilter(Consumer<ScriptEvent> consumer) {
        return LineFilter.notNulNotEmptyFilter(consumer);
    }

    protected Consumer<ScriptEvent> variableReplacer(Consumer<ScriptEvent> consumer) {
        if (variables != null && !variables.isEmpty()) {
            consumer = new VariableReplacer(variables, consumer);
        }
        return consumer;
    }

    @SuppressWarnings("unchecked")
    protected Consumer<ScriptStartEvent> scriptReader(Consumer<ScriptEvent> consumer) {
        if (scriptStore != null) {
            consumer = new ScriptStoreUpdater(scriptStore, consumer);
        }
        Consumer<ScriptStartEvent> startConsumer;
        if (scriptReaderIgnoreErrors == null) {
            startConsumer = new ScriptReader(consumer, charset);
        }  else {
            startConsumer = new ScriptReader(consumer, charset, scriptReaderIgnoreErrors);
        }
        if (scriptStore != null) {
            ScriptStoreFilter filter = new ScriptStoreFilter(scriptStore, startConsumer);
            if (scriptStoreReRunFailed != null) filter.setReRunFailed(scriptStoreReRunFailed);
            if (scriptStoreReRunChanged != null) filter.setReRunChanged(scriptStoreReRunChanged);
            if (scriptStoreReRunStartedTimeout != null) filter.setReRunStartedTimeout(scriptStoreReRunStartedTimeout);
            startConsumer = filter;
        }
        return startConsumer;
    }

    /**
     * Create consumer chain
     *
     * @return Head of consumer chain
     */
    public abstract Consumer<ScriptStartEvent> createConsumers();


    /**
     * Create consumer chain and scanner
     */
    public ScriptScanner createScriptScanner() {
        if (scriptScannerFactory == null) {
            throw new ConfigurationException("No scripts configured to run");
        }
        Consumer<ScriptStartEvent> consumer = createConsumers();
        return scriptScannerFactory.create(consumer);
    }

    public Pipeline build() {
        return new Pipeline(getScriptStore(), createScriptScanner());
    }
    /**
     * Create consumer chain and scanner, then run scanner
     */
    public void scan() throws IOException {
        try(Pipeline pipeline = build()) {
            pipeline.run();
        }
    }

    B withScriptScanner(ScriptScannerFactory scriptScriptScannerFactory) {
        this.scriptScannerFactory = scriptScriptScannerFactory;
        return self();
    }

    /**
     * Use single script
     */
    public B withScriptScanner(Script script) {
        return withScriptScanner(ScriptScannerFactories.script(script));
    }

    /**
     * Use single scripts from file system
     */
    public B withFileScriptScanner(Path file) {
        return withScriptScanner(ScriptScannerFactories.file(file));
    }

    /**
     * Use single scripts from class path
     */
    public B withResourceScriptScanner(Class clazz, String resource) {
        return withScriptScanner(ScriptScannerFactories.resource(clazz, resource));
    }

    /**
     * Use single scripts from class path
     */
    public B withResourceScriptScanner(ClassLoader classLoader, String resource) {
        return withScriptScanner(ScriptScannerFactories.resource(classLoader, resource));
    }

    /**
     * Scan and use multiples scripts from file system
     */
    public B withFilesScriptScanner(Path folder, Predicate<Path> fileFilter) {
        return withScriptScanner(ScriptScannerFactories.files(folder, fileFilter));
    }

    /**
     * Scan and use multiples scripts from file system using file glob syntax
     */
    public B withFilesScriptScanner(String fileGlob) {
        return withScriptScanner(ScriptScannerFactories.files(fileGlob));
    }

    /**
     * Scan and use multiple scripts from class path
     */
    public B withResourcesScriptScanner(ClassLoader classLoader, Predicate<String> resourceFilter) {
        return withScriptScanner(ScriptScannerFactories.resources(classLoader, resourceFilter));
    }

    /**
     * Scan and use multiple scripts from class path using resource glob syntax
     */
    public B withResourcesScriptScanner(ClassLoader classLoader, String resourceGlob) {
        return withScriptScanner(ScriptScannerFactories.resources(classLoader, resourceGlob));
    }

    /**
     * Scan and use multiple scripts from class path or file (dependending on scheme) using glob syntax.
     * Examples:<ul>
     *     <li>classpath:folder/script*.sql</li>
     *     <li>file:///folder/script*.sql</li>
     * </ul>
     */
    public B withSchemesScriptScanner(String glob) {
        return withScriptScanner(ScriptScannerFactories.schemes(glob));
    }

    public CompositeScriptScannerBuilder<B> withCompositeScriptScanner() {
        return new CompositeScriptScannerBuilder<>(self());
    }

    public static class CompositeScriptScannerBuilder<B extends PipelineBuilder<B>> extends CompositeScriptScanner.Builder<CompositeScriptScannerBuilder<B>> {
        private final B parentBuilder;

        public CompositeScriptScannerBuilder(B parentBuilder) {
            this.parentBuilder = parentBuilder;
        }

        public B end() {
            return parentBuilder.withScriptScanner(factory());
        }

    }
}
