package com.aixforce.seg;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cutter {
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("\\s+");

    private Pattern pattern;
    private boolean keep_delimiters;

    public Cutter(Pattern pattern, boolean keep_delimiters) {
        this.pattern = pattern;
        this.keep_delimiters = keep_delimiters;
    }

    public Cutter(String pattern, boolean keep_delimiters) {
        this(Pattern.compile(pattern == null ? "" : pattern), keep_delimiters);
    }

    public Cutter(Pattern pattern) {
        this(pattern, true);
    }

    public Cutter(String pattern) {
        this(pattern, true);
    }

    public Cutter(boolean keep_delimiters) {
        this(DEFAULT_PATTERN, keep_delimiters);
    }

    public Cutter() {
        this(DEFAULT_PATTERN);
    }

    public String[] split(String text) {
        if (text == null) {
            text = "";
        }

        int last_match = 0;
        LinkedList<String> splitted = new LinkedList<String>();

        Matcher m = this.pattern.matcher(text);

        while (m.find()) {

            splitted.add(text.substring(last_match, m.start()));

            if (this.keep_delimiters) {
                splitted.add(m.group());
            }

            last_match = m.end();
        }

        splitted.add(text.substring(last_match));

        return splitted.toArray(new String[splitted.size()]);
    }
}