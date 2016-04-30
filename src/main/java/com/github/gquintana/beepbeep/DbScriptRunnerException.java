package com.github.gquintana.beepbeep;

public class DbScriptRunnerException extends RuntimeException {
    public DbScriptRunnerException(String message) {
        super(message);
    }

    public DbScriptRunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
