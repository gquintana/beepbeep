package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.script.Script;

import java.io.*;
import java.nio.charset.Charset;

public class ScriptReaderProducer extends Producer implements Consumer<Script> {
    private final Charset charset;

    public ScriptReaderProducer(Consumer consumer, Charset charset) {
        super(consumer);
        this.charset = charset;
    }

    @Override
    public void consume(Script script) {
        produce(new ScriptEvent(script, ScriptEvent.Type.START));
        try(BufferedReader lineReader = new BufferedReader(new InputStreamReader(script.getStream(), charset))) {
            int lineNumber = 1;
            String line;
            while((line = lineReader.readLine()) != null) {
                produceLine(lineNumber, line);
            }
            produce(new ScriptEvent(script, ScriptEvent.Type.END_SUCCESS));
        } catch (Exception e) {
            produce(new ScriptEvent(script, ScriptEvent.Type.END_FAILED));
        }

    }

    private void produceLine(int lineNumber, String line) {
        produce(new LineEvent(lineNumber, line));
    }
}
