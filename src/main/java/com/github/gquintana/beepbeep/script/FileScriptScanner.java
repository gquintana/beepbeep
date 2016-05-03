package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

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
    private static final Pattern FILE_GLOB_PREFIX_PATTERN = Pattern.compile("^([^*]*\\/)?([^*\\/]*\\*.*)$");


    private final Path folder;
    private final Predicate<Path> fileFilter;
    private final int maxDepth;
    private static final Pattern AST_PATTERN = Pattern.compile("\\*+");

    public FileScriptScanner(Path folder, Predicate<Path> fileFilter, int maxDepth, Consumer<Script> scriptConsumer) {
        super(scriptConsumer);
        this.folder = folder;
        this.fileFilter = fileFilter;
        this.maxDepth = maxDepth;
    }
    public FileScriptScanner(Path folder, Predicate<Path> fileFilter, Consumer<Script> scriptConsumer) {
        this(folder, fileFilter, Integer.MAX_VALUE, scriptConsumer);
    }

    public void scan() throws IOException {
        ScriptsFileVisitor fileVisitor = new ScriptsFileVisitor();
        Files.walkFileTree(folder, EnumSet.of(FileVisitOption.FOLLOW_LINKS), maxDepth, fileVisitor);
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

    public static ScriptScanner fileGlob(String fileGlob, Consumer<Script> scriptConsumer) {
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
            Path folder = Paths.get(prefixSuffix[0] == null ? System.getProperty("user.dir") : prefixSuffix[0]);
            RegexPathPredicate fileFilter = new RegexPathPredicate(folder, fileGlobToRegex(prefixSuffix[1]));
            scanner = new FileScriptScanner(folder,
                fileFilter,
                deepSearch ? Integer.MAX_VALUE : 1,
                scriptConsumer);
        }
        return scanner;
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

    /**
     * Convert &ast;&ast;/&ast;.sql into a regex ^.&ast;/[^/]&ast;.sql$
     */
    static String fileGlobToRegex(String fileGlob) {
        String suffixRegex;
        StringBuilder suffixRegexBuilder = new StringBuilder("^");
        String[] suffixParts = fileGlob.split("/");
        for (int i = 0; i< suffixParts.length; i++) {
            String suffixPart = suffixParts[i];
            boolean lastPart = i == (suffixParts.length - 1);
            if (suffixPart.equals("**")) {
                if (lastPart) {
                    suffixRegexBuilder.append(".*");
                } else {
                    suffixRegexBuilder.append("(?:.*/)?");
                }
            } else {
                int start = 0;
                Matcher astMatcher = AST_PATTERN.matcher(suffixPart);
                while(astMatcher.find()) {
                    if (start < astMatcher.start()) {
                        suffixRegexBuilder.append(Pattern.quote(suffixPart.substring(start, astMatcher.start())));
                    }
                    suffixRegexBuilder.append("[^/]*");
                    start = astMatcher.end();
                }
                if (start < suffixPart.length()) {
                    suffixRegexBuilder.append(Pattern.quote(suffixPart.substring(start, suffixPart.length())));
                }
                if (!lastPart) {
                    suffixRegexBuilder.append("/");
                }
            }
        }
        suffixRegexBuilder.append('$');
        suffixRegex = suffixRegexBuilder.toString();
        return suffixRegex;
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
