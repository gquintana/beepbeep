package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileScriptScanner extends ScriptScanner {
    /**
     * Regex used to extract constant path at the beginning of a file glob
     */
    private static final Pattern FILE_GLOB_PREFIX_PATTERN = Pattern.compile("^([^*]*/)?([^*/]*\\*.*)$");

    /**
     * Folder from where are search files.
     */
    private final Path folder;
    private final Predicate<Path> fileFilter;
    private final int maxDepth;

    public FileScriptScanner(Path folder, Predicate<Path> fileFilter, int maxDepth, Consumer<ScriptStartEvent> scriptConsumer) {
        super(scriptConsumer);
        this.folder = folder;
        this.fileFilter = fileFilter;
        this.maxDepth = maxDepth;
    }

    public FileScriptScanner(Path folder, Predicate<Path> fileFilter, Consumer<ScriptStartEvent> scriptConsumer) {
        this(folder, fileFilter, Integer.MAX_VALUE, scriptConsumer);
    }

    @SuppressWarnings("Convert2MethodRef")
    public void scan() throws IOException {
        ScriptsFileVisitor fileVisitor = new ScriptsFileVisitor();
        Files.walkFileTree(folder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), maxDepth, fileVisitor);
        List<FileScript> scripts = fileVisitor.scripts;
        Collections.sort(scripts, Comparator.comparing(FileScript::getPath));
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

    /**
     * Create a ScriptScanner based on file globs
     */
    public static ScriptScanner fileGlob(String fileGlob, Consumer<ScriptStartEvent> scriptConsumer) {
        fileGlob = fixFileSeparator(fileGlob);
        String[] prefixSuffix = fileGlobPrefixSuffix(fileGlob);
        ScriptScanner scanner;
        if (prefixSuffix[1] == null) {
            // No *, no suffix
            Path path = Paths.get(prefixSuffix[0]);
            if (path.toFile().isFile()) {
                // Single file
                scanner = ScriptScanners.file(path, scriptConsumer);
            } else {
                // All files in given folder
                scanner = ScriptScanners.files(path, p -> true, scriptConsumer);
            }
        } else {
            // ** or *.sql suffix
            boolean deepSearch = fileGlob.contains("**");
            Path searchFolder = prefixSuffix[0] == null ? currentPath() : Paths.get(prefixSuffix[0]);
            RegexPathPredicate fileFilter = new RegexPathPredicate(searchFolder, fileGlobToRegex(prefixSuffix[1]));
            scanner = new FileScriptScanner(
                searchFolder,
                fileFilter,
                deepSearch ? Integer.MAX_VALUE : 1,
                scriptConsumer);
        }
        return scanner;
    }

    public static Path currentPath() {
        return Paths.get(System.getProperty("user.dir"));
    }

    /**
     * Split /var/lib/&ast;&ast;/&ast;.sql into /var/lib/ and &ast;&ast;/&ast;.sql
     */
    static String[] fileGlobPrefixSuffix(String fileGlob) {
        Matcher prefixSuffix = FILE_GLOB_PREFIX_PATTERN.matcher(fileGlob);
        String[] fileGlobParts = new String[2];
        if (prefixSuffix.matches()) {
            fileGlobParts[0] = prefixSuffix.group(1);
            fileGlobParts[1] = prefixSuffix.group(2);
        } else {
            fileGlobParts[0] = fileGlob;
        }
        return fileGlobParts;
    }

    private static final class RegexPathPredicate implements Predicate<Path> {
        private final Path folder;
        private final Pattern pattern;

        public RegexPathPredicate(Path folder, String regex) {
            this.folder = folder;
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public boolean test(Path path) {
            Path relativePath = folder == null ? path : folder.relativize(path);
            return pattern.matcher(fixFileSeparator(relativePath.toString())).matches();
        }
    }

}
