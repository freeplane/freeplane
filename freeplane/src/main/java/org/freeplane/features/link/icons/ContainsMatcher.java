package org.freeplane.features.link.icons;

import java.util.stream.Stream;

class ContainsMatcher implements DecorationRuleMatcher{
    private final String[] patterns;
    private final int totalLength;

    public ContainsMatcher(String pattern) {
        super();
        this.patterns = pattern.split("\\*");
        this.totalLength = Stream.of(patterns).mapToInt(String::length).sum();
    }

    @Override
    public int getMaximalMatchLength() {
        return totalLength;
    }

    @Override
    public int getMatchLength(String matchedString) {
        if(totalLength == 0)
            return 0;
        String startPattern = patterns[0];
        if(! matchedString.startsWith(startPattern))
            return 0;
        int lastPatternIndex = patterns.length - 1;
        if(lastPatternIndex == 0)
            return totalLength;
        String endPattern = patterns[lastPatternIndex];
        if(! matchedString.endsWith(endPattern))
            return 0;
        if(lastPatternIndex == 1)
            return totalLength;
        int startPosition = startPattern.length();
        int endPosition = matchedString.length() - endPattern.length();
        for(int position = startPosition, patternIndex = 1; 
                patternIndex < lastPatternIndex; 
                patternIndex++) {
            position = matchedString.indexOf(patterns[patternIndex], position);
            if(position < startPosition)
                return 0;
            position+=patterns[patternIndex].length();
            if(position > endPosition)
                return 0;
        }
        return totalLength;
    }

}
