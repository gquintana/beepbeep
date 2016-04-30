package com.github.gquintana.beepbeep.pipeline;

import java.io.*;
import java.nio.charset.Charset;

public class ScriptReaderProducer extends Producer {
    private final Reader reader;

    public ScriptReaderProducer(Consumer consumer, Reader reader) {
        super(consumer);
        this.reader = reader;
    }

    public ScriptReaderProducer(Consumer consumer, InputStream inputStream, Charset charset) {
        this(consumer, new BufferedReader(new InputStreamReader(inputStream, charset)));
    }

    public void produceAll() throws IOException {
        produce(new ScriptEvent(ScriptEvent.Type.START));
        try(BufferedReader lineReader = new BufferedReader(reader)) {
            int lineNumber = 1;
            String line;
            while((line = lineReader.readLine()) != null) {
                produceLine(lineNumber, line);
            }
            produce(new ScriptEvent(ScriptEvent.Type.END_SUCCESS));
        } catch (Exception e) {
            produce(new ScriptEvent(ScriptEvent.Type.END_FAILED));
        }
    }

    private void produceLine(int lineNumber, String line) {
        produce(new LineEvent(lineNumber, line));
    }
}
