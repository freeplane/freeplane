package org.freeplane.features.link.icons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PatternBasedMatcher implements DecorationRuleMatcher{
    private final Pattern pattern;

    public PatternBasedMatcher(Pattern pattern) {
        super();
        this.pattern = pattern;
    }
    
    @Override
    public int getMaximalMatchLength() {
        return Integer.MAX_VALUE;
    }


    @Override
    public int getMatchLength(String matchedString) {
        Matcher matcher = pattern.matcher(matchedString);
        return matcher.find() ? matcher.group().length() : 0;
    }

}
