/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry Polivaev
 *
 *  This file author is Felix Natter
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.filter;

import org.freeplane.core.resources.ResourceController;

public interface StringMatchingStrategy {
	
	double APPROXIMATE_MATCHING_MINPROB = ResourceController.getResourceController().getDoubleProperty("approximate_search_threshold");
	
	static final StringMatchingStrategy DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY = new PseudoDamerauLevenshtein();
	static final StringMatchingStrategy EXACT_STRING_MATCHING_STRATEGY = new ExactStringMatchingStrategy();
	
	/**
	 * Check for a match between a search term and a text.
	 * 
	 * @param searchTerm the text to search for
	 * @param searchText the text to search in
	 * @param subStringMatch whether to for substring instead of equality
	 * @param caseSensitive whether to honor case
	 * @return whether the configuration results in a match
	 */
	boolean matches(final String searchTerm, final String searchText, final boolean subStringMatch);
	
}
