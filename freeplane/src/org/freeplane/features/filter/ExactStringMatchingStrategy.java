package org.freeplane.features.filter;

public class ExactStringMatchingStrategy implements StringMatchingStrategy {

	public boolean matches(final String searchTerm, final String searchText,
			final boolean subStringMatch, final boolean caseSensitive) {
		
		if (subStringMatch)
		{
			return caseSensitive ? searchText.contains(searchTerm) :
				searchText.toLowerCase().contains(searchTerm.toLowerCase());  
		}
		else
		{
			return caseSensitive ? searchText.equals(searchTerm) :
				searchText.equalsIgnoreCase(searchTerm);
		}
	}

}
