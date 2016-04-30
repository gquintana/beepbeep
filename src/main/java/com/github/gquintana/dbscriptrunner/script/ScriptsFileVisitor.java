package com.github.gquintana.dbscriptrunner.script;

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

class ScriptsFileVisitor extends SimpleFileVisitor<Path> {
    private final List<FileScript> scripts = new ArrayList<>();
    private final Predicate<Path> fileFilter;
    public ScriptsFileVisitor(Predicate<Path> fileFilter) {
        this.fileFilter = fileFilter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && (fileFilter == null || fileFilter.test(file))) {
            scripts.add(new FileScript(file, attrs.size()));
        }
        return super.visitFile(file, attrs);
    }

    public static List<FileScript> walkTree(Path folder, Predicate<Path> fileFilter) throws IOException {
        ScriptsFileVisitor fileVisitor = new ScriptsFileVisitor(fileFilter);
        Files.walkFileTree(folder, fileVisitor);
        Collections.sort(fileVisitor.scripts, Comparator.comparing(file -> file.getPath()));
        return fileVisitor.scripts;
    }


}
