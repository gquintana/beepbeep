package com.github.gquintana.beepbeep.pipeline;

public interface Consumer<E> {
    void consume(E event);
}
