package com.github.gquintana.beepbeep.pipeline;

public abstract  class Producer<E> {
    protected final Consumer<E> consumer;

    protected Producer(Consumer<E> consumer) {
        this.consumer = consumer;
    }

    protected void produce(E event) {
        consumer.consume(event);
    }

    public Consumer<E> getConsumer() {
        return consumer;
    }
}
