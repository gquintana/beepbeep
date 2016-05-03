package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ScriptScanner {
    protected final Consumer<Script> scriptConsumer;

    protected ScriptScanner(Consumer<Script> scriptConsumer) {
        this.scriptConsumer = scriptConsumer;
    }

    protected void produce(Script script) {
        scriptConsumer.consume(script);
    }

    public abstract void scan() throws IOException;

    /**
     * Fix Windows file separator in paths
     */
    protected static String fixFileSeparator(String path) {
        return File.separatorChar == '/' ? path : path.replaceAll(Pattern.quote(File.separator), "/");
    }

    /**
     * Regex to find asterisks
     */
    private static final Pattern AST_PATTERN = Pattern.compile("\\*+");

    /**
     * Convert &ast;&ast;/&ast;.sql into a regex ^.&ast;/[^/]&ast;.sql$
     */
    protected static String fileGlobToRegex(String fileGlob) {
        String suffixRegex;
        StringBuilder suffixRegexBuilder = new StringBuilder("^");
        String[] suffixParts = fileGlob.split("/");
        for (int i = 0; i < suffixParts.length; i++) {
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
                while (astMatcher.find()) {
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

}
