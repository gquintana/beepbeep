package com.github.gquintana.dbscriptrunner.pipeline;

public abstract class LineTransformer extends Transformer {
    public LineTransformer(Consumer consumer) {
        super(consumer);
    }

    protected abstract String transform(String line);

    @Override
    protected Object transform(Object event) {
        if (!(event instanceof LineEvent)) {
            return event;
        }
        LineEvent lineEvent = (LineEvent) event;
        String line = lineEvent.getLine();
        String replaced = transform(line);
        if (replaced.equals(line)) {
            return event;
        }
        return new LineEvent(lineEvent.getLineNumber(), replaced);
    }

}
