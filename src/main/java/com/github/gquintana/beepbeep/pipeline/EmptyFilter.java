package com.github.gquintana.beepbeep.pipeline;

public class EmptyFilter<E> extends Filter<E> {
    public EmptyFilter(Consumer<E> consumer) {
        super(consumer);
    }

    @Override
    protected boolean filter(E event) {
        if (event == null) {
            return false;
        } else if (event instanceof String) {
            String string = (String) event;
            return !string.trim().isEmpty();
        } else {
            return true;
        }
    }
}
