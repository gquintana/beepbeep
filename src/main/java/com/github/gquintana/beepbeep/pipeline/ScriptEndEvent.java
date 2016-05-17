package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptEndEvent endEvent = (ScriptEndEvent) o;
        return lineNumber == endEvent.lineNumber &&
            Objects.equals(getScript(), endEvent.getScript()) &&
            Objects.equals(exception, endEvent.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, getScript(), exception);
    }
}
