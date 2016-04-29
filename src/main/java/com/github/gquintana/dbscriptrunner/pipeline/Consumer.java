package com.github.gquintana.dbscriptrunner.pipeline;

public interface Consumer<E> {
    void consume(E event);
}
