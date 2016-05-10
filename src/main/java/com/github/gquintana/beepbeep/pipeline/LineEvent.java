package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

import java.util.Objects;

public class LineEvent extends ScriptEvent {
    public static final String TYPE = "LINE";
    private final int lineNumber;
    private final String line;

    public LineEvent(Script script, int lineNumber, String line) {
        super(TYPE, script);
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLine() {
        return line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineEvent lineEvent = (LineEvent) o;
        return lineNumber == lineEvent.lineNumber &&
                Objects.equals(line, lineEvent.line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, line);
    }

    @Override
    protected StringBuilder toStringBuilder() {
        StringBuilder stringBuilder = super.toStringBuilder();
        stringBuilder.append(":").append(lineNumber).append(" ").append(line);
        return stringBuilder;
    }

}
