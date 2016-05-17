package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

import java.util.Objects;

public class ResultEvent extends ScriptEvent {
    public static final String TYPE = "RESULT";
    private final int lineNumber;
    private final String result;

    public ResultEvent(Script script, int lineNumber, String result) {
        super(TYPE, script);
        this.lineNumber = lineNumber;
        this.result = result;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultEvent lineEvent = (ResultEvent) o;
        return lineNumber == lineEvent.lineNumber &&
                Objects.equals(getScript(), lineEvent.getScript()) &&
                Objects.equals(result, lineEvent.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, getScript(), result);
    }

    @Override
    protected StringBuilder toStringBuilder() {
        StringBuilder stringBuilder = super.toStringBuilder();
        stringBuilder.append(":").append(lineNumber).append(" ").append(result);
        return stringBuilder;
    }

}
