package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.pipeline.Consumer;

public class PrintConsumer<E> implements Consumer<E> {
    @Override
    public void consume(E event) {
        if (event!=null) {
            System.out.println(event.toString());
        }
    }
}
