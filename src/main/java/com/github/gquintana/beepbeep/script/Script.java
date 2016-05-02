package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.BeepBeepException;

import java.io.IOException;
import java.io.InputStream;

public abstract class Script {
    protected Long size;

    public Script() {
    }

    protected Script(Long size) {
        this.size = size;
    }

    public abstract String getName();

    public abstract String getFullName();

    public abstract InputStream getStream() throws IOException;

    protected void analyze() throws IOException {
        try (InputStream inputStream = getStream()) {
            byte[] buffer = new byte[4096];
            int bufferLen;
            long currentSize = 0L;
            while ((bufferLen = inputStream.read(buffer)) >= 0) {
                currentSize += bufferLen;
            }
            size = currentSize;
        }
    }

    public long getSize() {
        if (size == null) {
            try {
                analyze();
            } catch (IOException e) {
                throw new BeepBeepException("Failed to get script " + getName() + " size", e);
            }
        }
        return size;
    }
}
