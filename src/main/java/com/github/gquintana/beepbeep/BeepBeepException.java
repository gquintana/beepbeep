package com.github.gquintana.beepbeep;

public class BeepBeepException extends RuntimeException {
    public BeepBeepException(String message) {
        super(message);
    }

    public BeepBeepException(String message, Throwable cause) {
        super(message, cause);
    }
}
