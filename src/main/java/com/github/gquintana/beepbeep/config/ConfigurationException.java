package com.github.gquintana.beepbeep.config;

import com.github.gquintana.beepbeep.BeepBeepException;

/**
 * Error while reading configuration file
 */
public class ConfigurationException extends BeepBeepException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
