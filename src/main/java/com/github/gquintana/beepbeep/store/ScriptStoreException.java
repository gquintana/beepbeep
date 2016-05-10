package com.github.gquintana.beepbeep.store;

import com.github.gquintana.beepbeep.BeepBeepException;

public class ScriptStoreException extends BeepBeepException {
    public ScriptStoreException(String message) {
        super(message);
    }

    public ScriptStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
