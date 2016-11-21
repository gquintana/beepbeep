package com.github.gquintana.beepbeep.elasticsearch;

public class RemoteElasticsearchException extends RuntimeException {
    public RemoteElasticsearchException(String message) {
        super(message);
    }

    public RemoteElasticsearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
