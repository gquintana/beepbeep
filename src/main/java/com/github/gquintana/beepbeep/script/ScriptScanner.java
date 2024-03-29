package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.Producer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ScriptScanner extends Producer<ScriptStartEvent> {

    protected ScriptScanner(Consumer<ScriptStartEvent> scriptConsumer) {
        super(scriptConsumer);
    }

    protected void produce(Script script) {
        produce(new ScriptStartEvent(script));
    }

    public abstract void scan() throws IOException;

    /**
     * Convert Windows file separator to Unix file separator
     */
    protected static String fixFileSeparator(String path) {
        return File.separatorChar == '/' ? path : path.replace(File.separatorChar, '/');
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
                    suffixRegexBuilder.append(Pattern.quote(suffixPart.substring(start)));
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
