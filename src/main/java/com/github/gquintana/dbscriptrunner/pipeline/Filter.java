package com.github.gquintana.dbscriptrunner.pipeline;

public abstract class Filter<E> extends Processor<E,E> {
    public Filter(Consumer<E> consumer) {
        super(consumer);
    }

    protected abstract boolean filter(E line);
    @Override
    public void consume(E event) {
        if (filter(event)) {
            produce(event);
        }
    }
}
