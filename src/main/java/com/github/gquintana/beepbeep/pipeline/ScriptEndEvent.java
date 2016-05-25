package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class ScriptEndEvent extends ScriptEvent {
    public static final String SUCCESS_TYPE = "END_SUCCESS";
    public static final String FAIL_TYPE = "END_FAIL";
    private final int lineNumber;
    private final Exception exception;
    private final Duration duration;

    public ScriptEndEvent(Script script, int lineNumber, Instant startInstant) {
        super(SUCCESS_TYPE, script);
        this.lineNumber = lineNumber;
        this.exception = null;
        this.duration = Duration.between(startInstant, getInstant());
    }

    public ScriptEndEvent(Script script, int lineNumber, Exception exception, Instant startInstant) {
        super(FAIL_TYPE, script);
        this.lineNumber = lineNumber;
        this.exception = exception;
        this.duration = Duration.between(startInstant, getInstant());
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
        stringBuilder.append(" ").append(duration);
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
