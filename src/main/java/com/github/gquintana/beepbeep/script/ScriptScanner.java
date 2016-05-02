package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public abstract class ScriptScanner {
    protected final Consumer<Script> scriptConsumer;

    protected ScriptScanner(Consumer<Script> scriptConsumer) {
        this.scriptConsumer = scriptConsumer;
    }

    protected void produce(Script script) {
        scriptConsumer.consume(script);
    }

    public abstract void scan() throws IOException;

    /**
     * Fix Windows file separator in paths
     */
    protected static String fixFileSeparator(String path) {
        return File.separatorChar == '/' ? path : path.replaceAll(Pattern.quote(File.separator), "/");
    }
}
