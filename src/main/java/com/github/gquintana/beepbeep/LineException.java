package com.github.gquintana.beepbeep;

import com.github.gquintana.beepbeep.pipeline.LineEvent;

public class LineException extends BeepBeepException {
    private final LineEvent lineEvent;

    public LineException(String message, LineEvent lineEvent) {
        super(message);
        this.lineEvent = lineEvent;
    }

    public LineException(String message, Throwable cause, LineEvent lineEvent) {
        super(message, cause);
        this.lineEvent = lineEvent;
    }

    public LineEvent getLineEvent() {
        return lineEvent;
    }
}
