package com.github.gquintana.beepbeep.pipeline;

import java.util.function.Predicate;

public class LineFilter<E> extends Filter<E> {
    private final Predicate<String> lineFilter;

    public LineFilter(Predicate<String> lineFilter, Consumer<E> consumer) {
        super(consumer);
        this.lineFilter = lineFilter;
    }

    @Override
    protected boolean filter(E event) {
        if (event instanceof LineEvent) {
            LineEvent lineEvent = (LineEvent) event;
            return filter(lineEvent.getLine());
        } else {
            return true;
        }
    }

    private boolean filter(String line) {
        return lineFilter.test(line);
    }

    public static boolean isNotNullNorEmpty(String line) {
        return line != null && !line.trim().isEmpty();
    }

    public  static <E> LineFilter<E> notNulNotEmptyFilter(Consumer<E> consumer) {
        return new LineFilter<E>(LineFilter::isNotNullNorEmpty, consumer);
    }

}
