package org.freeplane.features.filter;

public class ExactStringMatchingStrategy implements StringMatchingStrategy {

	public boolean matches(final String searchTerm, final String searchText,
			final boolean subStringMatch) {
		
		if (subStringMatch)
		{
			return searchText.contains(searchTerm);  
		}
		else
		{
			return searchText.equals(searchTerm);
		}
	}

}
