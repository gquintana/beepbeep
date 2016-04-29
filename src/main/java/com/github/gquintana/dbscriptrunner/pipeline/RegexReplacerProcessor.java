package com.github.gquintana.dbscriptrunner.pipeline;

import java.util.regex.Pattern;

public class RegexReplacerProcessor extends LineTransformer {
    private final Pattern searchPattern;
    private final String replacement;

    public RegexReplacerProcessor(Pattern searchPattern, String replacement, Consumer consumer) {
        super(consumer);
        this.searchPattern = searchPattern;
        this.replacement = replacement;
    }

    public RegexReplacerProcessor(String searchRegex, String replacement, Consumer consumer) {
        this(Pattern.compile(searchRegex, Pattern.MULTILINE), replacement, consumer);
    }

    @Override
    protected String transform(String line) {
        return replace(line);
    }

    private String replace(String line) {
        return searchPattern.matcher(line).replaceAll(replacement);
    }
}
