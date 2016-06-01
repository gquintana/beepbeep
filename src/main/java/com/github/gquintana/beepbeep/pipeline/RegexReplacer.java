package com.github.gquintana.beepbeep.pipeline;

import java.util.regex.Pattern;

public class RegexReplacer extends LineTransformer {
    private final Pattern searchPattern;
    private final String replacement;

    public RegexReplacer(Pattern searchPattern, String replacement, Consumer<ScriptEvent> consumer) {
        super(consumer);
        this.searchPattern = searchPattern;
        this.replacement = replacement;
    }

    public RegexReplacer(String searchRegex, String replacement, Consumer<ScriptEvent> consumer) {
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
