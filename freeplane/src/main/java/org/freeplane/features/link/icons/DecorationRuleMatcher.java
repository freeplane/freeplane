package org.freeplane.features.link.icons;

public interface DecorationRuleMatcher {
    int getMatchLength(String matchedString);
    int getMaximalMatchLength();
}
