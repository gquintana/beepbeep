package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.script.Script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ScriptReader extends Producer<ScriptEvent> implements Consumer<ScriptStartEvent> {
    private final Charset charset;
    private final boolean ignoreErrors;

    public ScriptReader(Consumer<ScriptEvent> consumer, Charset charset, boolean ignoreErrors) {
        super(consumer);
        this.charset = charset;
        this.ignoreErrors = ignoreErrors;
    }

    public ScriptReader(Consumer<ScriptEvent> consumer, Charset charset) {
        this(consumer, charset, false);
    }

    @Override
    public void consume(ScriptStartEvent event) {
        Script script = event.getScript();
        produce(event);
        int lineNumber = 1;
        String line;
        try (BufferedReader lineReader = new BufferedReader(new InputStreamReader(script.getStream(), charset))) {
            Exception firstException = null;
            while ((line = lineReader.readLine()) != null) {
                if (ignoreErrors) {
                    try {
                        produceLine(script, lineNumber, line);
                    } catch (Exception e) {
                        if (firstException == null) {
                            firstException = e;
                        }
                        produce(new ResultEvent(script, lineNumber, "ERROR " + e.getMessage()));
                    }
                } else {
                    produceLine(script, lineNumber, line);
                }
                lineNumber++;
            }
            if (firstException == null) {
                produceEndSuccess(script, lineNumber, event);
            } else {
                produceEndFail(script, lineNumber, firstException, event);
            }
        } catch (IOException e) {
            produceEndFail(script, lineNumber, e, event);
            throw new BeepBeepException("I/O failure reading " + script.getName(), e);
        } catch (RuntimeException e) {
            produceEndFail(script, lineNumber, e, event);
            throw e;
        }

    }

    private void produceLine(Script script, int lineNumber, String line) {
        produce(new LineEvent(script, lineNumber, line));
    }

    private void produceEndSuccess(Script script, int lineNumber, ScriptStartEvent event) {
        produce(new ScriptEndEvent(script, lineNumber, event.getInstant()));
    }

    private void produceEndFail(Script script, int lineNumber, Exception exception, ScriptStartEvent event) {
        produce(new ScriptEndEvent(script, lineNumber, exception, event.getInstant()));
    }

}
