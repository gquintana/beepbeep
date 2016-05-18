package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.script.Script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ScriptReaderProducer extends Producer<ScriptEvent> implements Consumer<ScriptStartEvent> {
    private final Charset charset;

    public ScriptReaderProducer(Consumer<ScriptEvent> consumer, Charset charset) {
        super(consumer);
        this.charset = charset;
    }

    @Override
    public void consume(ScriptStartEvent event) {
        Script script = event.getScript();
        produce(event);
        int lineNumber = 1;
        String line;
        try (BufferedReader lineReader = new BufferedReader(new InputStreamReader(script.getStream(), charset))) {
            while ((line = lineReader.readLine()) != null) {
                produce(new LineEvent(script, lineNumber, line));
                lineNumber++;
            }
            produce(new ScriptEndEvent(script, lineNumber));
        } catch (IOException e) {
            produce(new ScriptEndEvent(script, lineNumber, e));
            throw new BeepBeepException("I/O failure reading " + script.getName(), e);
        } catch (RuntimeException e) {
            produce(new ScriptEndEvent(script, lineNumber, e));
            throw e;
        }

    }

}
