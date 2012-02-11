package org.freeplane.features.filter;

public interface StringMatchingStrategy {
	
	float APPROXIMATE_MATCHING_MINPROB = 0.7F;
	
	StringMatchingStrategy DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY = new DamerauLevenshtein();
	
	/**
	 * Check for a match between a search term and a text.
	 * 
	 * @param searchTerm the text to search for
	 * @param searchText the text to search in
	 * @param subStringMatch whether to for substring instead of equality
	 * @param caseSensitive whether to honor case
	 * @return whether the configuration results in a match
	 */
	boolean matches(final String searchTerm, final String searchText, final boolean subStringMatch,
			final boolean caseSensitive);
	
}
