package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class FileScriptScanner extends ScriptScanner {

    private final Path folder;
    private final Predicate<Path> fileFilter;

    public FileScriptScanner(Path folder, Predicate<Path> fileFilter, Consumer<Script> scriptConsumer) {
        super(scriptConsumer);
        this.folder = folder;
        this.fileFilter = fileFilter;
    }

    public void scan() throws IOException {
        ScriptsFileVisitor fileVisitor = new ScriptsFileVisitor();
        Files.walkFileTree(folder, fileVisitor);
        List<FileScript> scripts = fileVisitor.scripts;
        Collections.sort(scripts, Comparator.comparing(file -> file.getPath()));
        for (FileScript script : scripts) {
            produce(script);
        }
    }

    private class ScriptsFileVisitor extends SimpleFileVisitor<Path> {
        private final List<FileScript> scripts = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile() && (fileFilter == null || fileFilter.test(file))) {
                scripts.add(new FileScript(file, attrs.size()));
            }
            return super.visitFile(file, attrs);
        }
    }
}
