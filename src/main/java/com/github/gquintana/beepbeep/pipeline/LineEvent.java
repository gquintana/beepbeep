package com.github.gquintana.beepbeep.pipeline;

import java.util.Objects;

public class LineEvent {
    private final int lineNumber;
    private final String line;

    public LineEvent(int lineNumber, String line) {
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
    public String toString() {
        return "line " + lineNumber + ": " + line;
    }

    public static LineEvent event(int lineNumber, String line) {
        return new LineEvent(lineNumber, line);
    }
}
