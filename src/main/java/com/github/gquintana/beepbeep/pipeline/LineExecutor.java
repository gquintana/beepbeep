package com.github.gquintana.beepbeep.pipeline;

/**
 * Runs given LineEvent
 */
public abstract class LineExecutor extends Processor<ScriptEvent, ScriptEvent> {
    public LineExecutor(Consumer consumer) {
        super(consumer);
    }

    @Override
    public void consume(ScriptEvent event) {
        if (event instanceof LineEvent) {
            executeLine((LineEvent) event);
        } else {
            ScriptEvent scriptEvent = (ScriptEvent) event;
            switch (scriptEvent.getType()) {
                case ScriptStartEvent.TYPE:
                    executeStart();
                    break;
                case ScriptEndEvent.SUCCESS_TYPE:
                    executeEnd(true);
                    break;
                case ScriptEndEvent.FAIL_TYPE:
                    executeEnd(false);
            }
            produce(event);
        }
    }

    private void executeEnd(boolean success) {

    }

    protected void executeStart() {

    }

    protected abstract void executeLine(LineEvent event);

    protected void produce(LineEvent lineEvent, String result) {
        produce(new ResultEvent(lineEvent.getScript(), lineEvent.getLineNumber(), result));
    }
}
