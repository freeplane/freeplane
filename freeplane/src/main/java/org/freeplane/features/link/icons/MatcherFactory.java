package org.freeplane.features.link.icons;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class MatcherFactory {
    static final MatcherFactory INSTANCE = new MatcherFactory();
    private Map<String, DecorationRuleMatcher> matcherCache;
    
    MatcherFactory() {
        matcherCache  = new HashMap<>();
    }
    DecorationRuleMatcher matcherOf(String specification) {
        return matcherCache.computeIfAbsent(specification.trim(), this::createMatcher);
    }
    
    private DecorationRuleMatcher createMatcher(String specification) {
        if(specification.startsWith("{") && specification.endsWith("}"))
            return new PatternBasedMatcher(Pattern.compile(specification.substring(1, specification.length() - 1)));
        else
            return new ContainsMatcher(specification);
    }
}
