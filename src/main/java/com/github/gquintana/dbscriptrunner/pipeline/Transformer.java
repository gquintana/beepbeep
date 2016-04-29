package com.github.gquintana.dbscriptrunner.pipeline;

public abstract class Transformer<I,O> extends Processor<I,O> {
    protected Transformer(Consumer consumer) {
        super(consumer);
    }

    @Override
    public void consume(I event) {
        produce(transform(event));
    }

    protected abstract O transform(I event);
}
