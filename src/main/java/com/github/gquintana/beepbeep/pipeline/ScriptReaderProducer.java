package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

import java.io.*;
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
        try(BufferedReader lineReader = new BufferedReader(new InputStreamReader(script.getStream(), charset))) {
            while((line = lineReader.readLine()) != null) {
                produce(new LineEvent(script, lineNumber, line));
            }
            produce(new ScriptEndEvent(script, lineNumber));
        } catch (Exception e) {
            produce(new ScriptEndEvent(script, lineNumber, e));
        }

    }

}
