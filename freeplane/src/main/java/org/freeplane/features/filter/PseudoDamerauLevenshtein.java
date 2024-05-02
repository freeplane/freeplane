/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry Polivaev
 *
 *  This file's author is Felix Natter
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

import java.util.Arrays;

/**
 * Pseudo-Damerau-Levenshtein (aka "Optimal String Distance")
 * implementation which allows some non-adjacent transpositions(?)
 * Computes the edit distance with insertions/deletions/substitutions/transpositions.
 *
 * Optionally the edit distance of a semi-global alignment is computed which
 * allows the search term to be shifted free-of-cost (i.e. dist("file", "a file is")==0).
 *
 * Some properties are explained in the unit test, {@link org.freeplane.features.filter.EditDistanceStringMatchingStrategiesTest}.
 *
 * TODO: use unicode code points instead of chars !!
 * (but neither simplyhtml nor freeplane are currently codepoint-safe...)
 *
 * @author Felix Natter <fnatter@gmx.net>
 *
 */
public class PseudoDamerauLevenshtein implements EditDistanceStringMatchingStrategy {
	private int[][] matrix;
	private String searchTerm;
	private String searchText;
	private final int costIndel = 1;
	private final int costMismatch = 1;
	private final int costTranspos = 1;
	private Type type;

    private boolean isWordBegin(int position) {
        if (position == 0 || position == searchText.length()) return true; // Start or end of string
        return Character.isLetterOrDigit(searchText.charAt(position)) &&
               ! Character.isLetterOrDigit(searchText.charAt(position - 1));
    }
    private boolean isWordEnd(int position) {
        if (position == 0 || position == searchText.length()) return true; // Start or end of string
        return Character.isLetterOrDigit(searchText.charAt(position - 1)) &&
               ! Character.isLetterOrDigit(searchText.charAt(position));
    }

	private boolean isMatch(int i, int j)
	{
		char col = searchTerm.charAt(i-1);
		char row = searchText.charAt(j-1);
		if (col == row || row == '-')
			return true;
		else
			return false;
	}

	@Override
    public int distance() {

		matrix = new int[searchTerm.length()+1][searchText.length()+1]; // [row][col]

		 // first column: start-gap penalties for searchTerm
		for (int i = 0; i <= searchTerm.length(); i++)
			matrix[i][0] = i*costIndel;

		// first row: start-gap penalties for searchText
		if (type == Type.ALL)
		{
			for (int j = 1; j <= searchText.length(); j++)
				matrix[0][j] = j*costIndel;
		}
		else if (type == Type.WORDWISE)
        {
            for (int j = 1, letterCounter = 1; j <= searchText.length(); j++, letterCounter++) {
                if(isWordBegin(j))
                    letterCounter = 0;
                matrix[0][j] = letterCounter*costIndel;
            }
        }
        else if (type == Type.SUBSTRING)
		{
			Arrays.fill(matrix[0], 0);
		}

		// compute the rest of the matrix
		for (int i = 1; i <= searchTerm.length(); i++)
		{
			for (int j = 1; j <= searchText.length(); j++)
			{
				int cost_try_match = matrix[i-1][j-1] + (isMatch(i,j) ? 0 : costMismatch);
				int cost_ins = matrix[i-1][j] + costIndel;
				int cost_del = matrix[i][j-1] + costIndel;
				matrix[i][j] = Math.min(cost_try_match, Math.min(cost_ins, cost_del));

				if (i >= 2 && j >= 2 &&
					    searchTerm.charAt(i-2) == searchText.charAt(j-1) &&
					    searchTerm.charAt(i-1) == searchText.charAt(j-2))
				{
					matrix[i][j] = Math.min(matrix[i][j], matrix[i-2][j-2] + costTranspos);
				}
		  	}
		}
		//writeMatrix(matrix);
		if (type == Type.ALL)
		{
			return matrix[searchTerm.length()][searchText.length()];
		}
		else if (type == Type.WORDWISE) {
            int min = Integer.MAX_VALUE;
            for (int j = searchText.length(), letterCounter = 0; j >= 0; j--, letterCounter++)
            {
                if(isWordEnd(j))
                    letterCounter = 0;
                min = Math.min(min, matrix[searchTerm.length()][j] + letterCounter * costIndel);
            }
            return min;

		}
		else // if (type == Type.SUBSTRING)
		{
			int min = Integer.MAX_VALUE;
			for (int j = 0; j <= searchText.length(); j++)
			{
				min = Math.min(min, matrix[searchTerm.length()][j]);
			}
			return min;
		}

	}
	private float getMatchProb(final int distance)
	{
		if (type == Type.ALL) {
			return 1.0F - ((float)distance / Math.min(searchTerm.length(), searchText.length()));
		} else {
			return 1.0F - ((float)distance / searchTerm.length());
		}
	}

	@Override
    public float matchProb()
	{
		//LogUtils.severe("minMatchProb=" +StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB);
		int dist = distance();
		matrix = null;
		//LogUtils.severe(String.format("DLevDist(%s,%s) = %d\n", searchTerm, searchText, dist));
		return getMatchProb(dist);
	}

	public PseudoDamerauLevenshtein() {
		//LogUtils.severe("minMatchProb=" +StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB);
	}

	@Override
    public void init(String searchTerm, String searchText,
			Type matchType)
	{
		if (searchTerm == null || searchText == null)
		{
			throw new IllegalArgumentException("Null searchText/searchTerm!");
		}

        this.searchTerm = searchTerm;
        this.searchText = searchText;
 		this.type = matchType;
	}

	@Override
    public boolean matches(String searchTerm, String searchText,
            Type matchType)
	{
		init(searchTerm, searchText, matchType);

		return matchProb() > StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB;
	}
}
