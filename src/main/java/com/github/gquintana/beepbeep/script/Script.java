package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.util.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

public abstract class Script {
    private boolean analyzed;
    protected Long size;
    protected String sha1Hex;
    private final ScriptConfiguration configuration = new ScriptConfiguration();

    public Script() {
    }

    protected Script(Long size) {
        this.size = size;
    }

    public abstract String getName();

    public abstract String getFullName();

    public abstract InputStream getStream() throws IOException;

    /**
     * Analyze script and extract, size, checksum and configuration
     */
    protected final void analyze() {
        if (analyzed) {
            return;
        }
        try (InputStream inputStream = getStream();
             AnalyzerInputStream analyzerInputStream = new AnalyzerInputStream(inputStream, "SHA-1");
             InputStreamReader reader = new InputStreamReader(analyzerInputStream, "UTF-8");
             BufferedReader lineReader = new BufferedReader(reader)) {
            String line;
            while ((line = lineReader.readLine()) != null) {
                configuration.parse(line);
            }
            size = analyzerInputStream.getSize();
            sha1Hex = Strings.bytesToHex(analyzerInputStream.getDigest());
            analyzed = true;
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
        analyze();
        return sha1Hex;
    }

    public <T> Optional<T> getConfiguration(String name, Class<T> type) {
        analyze();
        return configuration.getValue(name, type);
    }

    public <T> void setConfiguration(String name, T value) {
        configuration.setValue(name, value);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Script script = (Script) o;
        return Objects.equals(getFullName(), script.getFullName()) &&
            Objects.equals(getSize(), script.getSize()) &&
            Objects.equals(getSha1Hex(), script.getSha1Hex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFullName(), size, sha1Hex);
    }
}
