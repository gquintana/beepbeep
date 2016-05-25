package com.github.gquintana.beepbeep.cli;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.gquintana.beepbeep.pipeline.ResultEvent;

public class PrintConsumer<E> implements Consumer<E> {
    /**
     * Print results
     */
    private final boolean verbose;

    public PrintConsumer(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void consume(E event) {
        if (event!=null) {
            if (verbose || (!(event instanceof ResultEvent || event instanceof LineEvent))) {
                System.out.println(event.toString());
            }
        }
    }
}
