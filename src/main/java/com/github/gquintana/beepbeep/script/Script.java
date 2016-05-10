package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Script {
    protected Long size;
    protected String sha1Hex;

    public Script() {
    }

    protected Script(Long size) {
        this.size = size;
    }

    public abstract String getName();

    public abstract String getFullName();

    public abstract InputStream getStream() throws IOException;

    protected final void analyze() {
        try (InputStream inputStream = getStream()) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[4096];
            int bufferLen;
            long currentSize = 0L;
            while ((bufferLen = inputStream.read(buffer)) >= 0) {
                currentSize += bufferLen;
                md.update(buffer, 0, bufferLen);
            }
            size = currentSize;
            sha1Hex = Strings.bytesToHex(md.digest());
        } catch (IOException e) {
            throw new BeepBeepException("Failed to get script " + getName() + " size", e);
        } catch (NoSuchAlgorithmException e) {
            throw new BeepBeepException("Failed to get script " + getName() + " SHA-1", e);
        }
    }

    public long getSize() {
        if (size == null) {
            analyze();
        }
        return size;
    }

    public String getSha1Hex() {
        if (sha1Hex == null) {
            analyze();
        }
        return sha1Hex;
    }
}
