package org.freeplane.features.filter;

import java.util.regex.Pattern;

public class ExactStringMatchingStrategy implements StringMatchingStrategy {

	public boolean matches(final String searchTerm, final String searchText,
			Type matchType) {
		switch(matchType) {
        case ALL:
            return searchText.equals(searchTerm);
        case WORDWISE:
            return Pattern.compile("\\b" + Pattern.quote(searchTerm) + "\\b").matcher(searchText).find();
        case SUBSTRING:
            return searchText.contains(searchTerm);
		}
		return false;
	}

}
