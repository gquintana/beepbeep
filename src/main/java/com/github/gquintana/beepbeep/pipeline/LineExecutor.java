package com.github.gquintana.beepbeep.pipeline;

/**
 * Runs given LineEvent
 */
public abstract class LineExecutor extends Processor {
    public LineExecutor(Consumer consumer) {
        super(consumer);
    }
    @Override
    public void consume(Object event) {
        if (event instanceof LineEvent) {
            executeLine((LineEvent) event);
        } else if (event instanceof ScriptEvent) {
            ScriptEvent scriptEvent = (ScriptEvent) event;
            switch (scriptEvent.getType()) {
                case START:
                    executeStart();
                    break;
                case END_SUCCESS:
                    executeEnd(true);
                    break;
                case END_FAILED:
                    executeEnd(false);
            }
            produce(event);
        } else {
            produce(event);
        }
    }

    private void executeEnd(boolean success) {

    }

    protected void executeStart() {

    }

    protected abstract void executeLine(LineEvent event);

}
