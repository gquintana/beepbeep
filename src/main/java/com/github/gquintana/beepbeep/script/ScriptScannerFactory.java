package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

/**
 * Factory method of {@link ScriptScanner} implementation
 */
@FunctionalInterface
public interface ScriptScannerFactory<S extends ScriptScanner> {
    S create(Consumer<ScriptStartEvent> consumer);
}
