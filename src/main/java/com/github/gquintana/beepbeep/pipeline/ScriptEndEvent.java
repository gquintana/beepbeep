package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

public class ScriptEndEvent extends ScriptEvent {
    public static final String SUCCESS_TYPE = "END_SUCCESS";
    public static final String FAIL_TYPE = "END_FAIL";
    private final int lineNumber;
    private final Exception exception;

    public ScriptEndEvent(Script script, int lineNumber) {
        super(SUCCESS_TYPE, script);
        this.lineNumber = lineNumber;
        this.exception = null;
    }

    public ScriptEndEvent(Script script, int lineNumber, Exception exception) {
        super(FAIL_TYPE, script);
        this.lineNumber = lineNumber;
        this.exception = exception;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    protected StringBuilder toStringBuilder() {
        StringBuilder stringBuilder = super.toStringBuilder();
        stringBuilder.append(":").append(lineNumber);
        if (exception != null) {
            stringBuilder.append(" ").append(exception.getMessage());
        }
        return stringBuilder;
    }

    public boolean isSuccess() {
        return exception == null;
    }
}
