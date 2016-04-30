package com.github.gquintana.beepbeep.pipeline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aggregate multiple line statements
 */
public class MultilineAggregator extends Processor {
    /**
     * Pattern to mark statement end or statement continuation
     */
    private final Pattern lineMarkerPattern;
    private final LineMarkerStrategy lineMarkerStrategy;
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

    public MultilineAggregator(Pattern lineMarkerPattern, LineMarkerStrategy lineMarkerStrategy, Consumer consumer) {
        super(consumer);
        this.lineMarkerPattern = lineMarkerPattern;
        this.lineMarkerStrategy = lineMarkerStrategy;
    }

    public MultilineAggregator(String endOfLineRegex, LineMarkerStrategy lineMarkerStrategy, Consumer consumer) {
        this(Pattern.compile(endOfLineRegex), lineMarkerStrategy, consumer);
    }

    public MultilineAggregator(String endOfLineRegex, Consumer consumer) {
        this(endOfLineRegex, LineMarkerStrategy.END, consumer);
    }

    public void consume(Object event) {
        if (! (event instanceof LineEvent)) {
            // End Of file
            flush(lastLineEvent);
            produce(event);
            return;
        }
        LineEvent lineEvent = (LineEvent) event;
        String line = lineEvent.getLine();
        Matcher lineMarkerMatcher = lineMarkerPattern.matcher(line);
        boolean lineMarkerFound = lineMarkerMatcher.find();
        if (lineMarkerFound && lineMarkerStrategy == LineMarkerStrategy.START) {
            flush(lastLineEvent);
        }
        append(line, lineEvent);
        if (lineMarkerFound && lineMarkerStrategy == LineMarkerStrategy.END) {
            flush(lineEvent);
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
            produce(new LineEvent(event.getLineNumber(), multiLine));
        }
    }
}
