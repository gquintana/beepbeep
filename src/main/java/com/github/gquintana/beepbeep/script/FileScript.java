package com.github.gquintana.beepbeep.script;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Script loaded from file system
 */
public class FileScript extends Script {
    private final Path path;

    public FileScript(Path path) {
        this.path = path;
    }


    public FileScript(Path path, Long size) {
        super(size);
        this.path = path;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String getFullName() {
        return path.toString();
    }

    @Override
    public InputStream getStream() throws IOException {
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    public Path getPath() {
        return path;
    }

}
