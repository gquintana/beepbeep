package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.util.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aggregate multiple line statements
 */
public class MultilineAggregator extends Processor<ScriptEvent,ScriptEvent> {
    /**
     * Pattern to mark statement end or statement continuation
     */
    private final Pattern lineMarkerPattern;
    private final LineMarkerStrategy lineMarkerStrategy;
    private final boolean removeLineMarker;

    public enum LineMarkerStrategy {
        START, END
    }

    /**
     * End of line character(s)
     */
    private final String endOfLineChar = System.lineSeparator();
    /**
     * Statement being read
     */
    private StringBuilder lineBuilder;
    private LineEvent lastLineEvent;

    public MultilineAggregator(Pattern lineMarkerPattern, LineMarkerStrategy lineMarkerStrategy, boolean removeLineMarker, Consumer<ScriptEvent> consumer) {
        super(consumer);
        this.lineMarkerPattern = lineMarkerPattern;
        this.lineMarkerStrategy = lineMarkerStrategy;
        this.removeLineMarker = removeLineMarker;
    }

    public MultilineAggregator(String endOfLineRegex, LineMarkerStrategy lineMarkerStrategy, boolean removeLineMarker, Consumer<ScriptEvent> consumer) {
        this(Pattern.compile(endOfLineRegex), lineMarkerStrategy, removeLineMarker, consumer);
    }

    public MultilineAggregator(String endOfLineRegex, Consumer<ScriptEvent> consumer) {
        this(endOfLineRegex, LineMarkerStrategy.END, true, consumer);
    }

    public void consume(ScriptEvent event) {
        if (!(event instanceof LineEvent)) {
            // End Of file
            flush(lastLineEvent);
            produce(event);
            return;
        }
        LineEvent lineEvent = (LineEvent) event;
        String line = lineEvent.getLine();
        Matcher lineMarkerMatcher = lineMarkerPattern.matcher(line);
        boolean lineMarkerFound = lineMarkerMatcher.find();
        if (lineMarkerFound) {
            String before = Strings.left(line, lineMarkerMatcher.start());
            String marker = line.substring(lineMarkerMatcher.start(), lineMarkerMatcher.end());
            String after = Strings.right(line, lineMarkerMatcher.end());
            if (lineMarkerStrategy == LineMarkerStrategy.START) {
                if (!before.isEmpty()) {
                    append(before, lineEvent);
                }
                flush(lastLineEvent);
                if (removeLineMarker) {
                    append(after, lineEvent);
                } else {
                    append(marker + after, lineEvent);
                }
            } else if (lineMarkerStrategy == LineMarkerStrategy.END) {
                if (removeLineMarker) {
                    append(before, lineEvent);
                } else {
                    append(before + marker, lineEvent);
                }
                flush(lastLineEvent);
                if (!after.isEmpty()) {
                    append(after, lineEvent);
                }
            }
        } else {
            append(line, lineEvent);
        }
    }

    private void append(String line, LineEvent lineEvent) {
        if (lineBuilder == null) {
            lineBuilder = new StringBuilder(line);
        } else {
            lineBuilder.append(line);
        }
        lineBuilder.append(endOfLineChar);
        lastLineEvent = lineEvent;
    }

    private void flush(LineEvent event) {
        if (lineBuilder != null) {
            String multiLine = lineBuilder.toString();
            lineBuilder = null;
            lastLineEvent = null;
            produce(new LineEvent(event.getScript(), event.getLineNumber(), multiLine));
        }
    }
}
