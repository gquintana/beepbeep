package com.github.gquintana.beepbeep.pipeline;

public abstract class LineTransformer extends Transformer<ScriptEvent, ScriptEvent> {
    public LineTransformer(Consumer<ScriptEvent> consumer) {
        super(consumer);
    }

    protected abstract String transform(String line);

    @Override
    protected ScriptEvent transform(ScriptEvent event) {
        if (!(event instanceof LineEvent)) {
            return event;
        }
        LineEvent lineEvent = (LineEvent) event;
        String line = lineEvent.getLine();
        String replaced = transform(line);
        if (replaced.equals(line)) {
            return event;
        }
        return new LineEvent(lineEvent.getScript(), lineEvent.getLineNumber(), replaced);
    }

}
