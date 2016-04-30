package com.github.gquintana.beepbeep.pipeline;

public abstract class Processor<I,O> extends Producer<O> implements Consumer<I> {
    public Processor(Consumer consumer) {
        super(consumer);
    }
}
