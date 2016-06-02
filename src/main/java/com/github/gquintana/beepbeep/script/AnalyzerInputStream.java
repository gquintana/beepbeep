package com.github.gquintana.beepbeep.script;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * FilterInputStream which computes size and  checksum
 */
public class AnalyzerInputStream extends FilterInputStream {
    private final MessageDigest md;
    private long size;
    public AnalyzerInputStream(InputStream in, String digestAlgorithm) throws NoSuchAlgorithmException {
        super(in);
        md = MessageDigest.getInstance(digestAlgorithm);
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b >= 0) {
            size++;
            md.update((byte) b);
        }
        return b;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int l = super.read(b);
        if (l >= 0) {
            size += l;
            md.update(b);
        }
        return l;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int l = super.read(b, off, len);
        if (l >= 0) {
            size += l;
            md.update(b, off, len);
        }
        return super.read(b, off, len);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    public long getSize() {
        return size;
    }

    public byte[] getDigest() {
        return md.digest();
    }
}
