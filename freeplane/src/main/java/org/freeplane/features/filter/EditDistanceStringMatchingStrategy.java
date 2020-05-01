package org.freeplane.features.filter;

public interface EditDistanceStringMatchingStrategy extends
		StringMatchingStrategy {

	public enum Type { Global, SemiGlobal };
	
	int distance();
	
	float matchProb();
	
	void init(final String searchTerm, final String searchText, final boolean subStringMatch);
	
}

