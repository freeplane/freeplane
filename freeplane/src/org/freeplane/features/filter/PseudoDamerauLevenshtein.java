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

import org.freeplane.core.util.LogUtils;

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
 * 
 * @author Felix Natter <fnatter@gmx.net>
 *
 */
public class PseudoDamerauLevenshtein implements EditDistanceStringMatchingStrategy {
	private String searchTerm;
	private String searchText;
	private final int costIndel = 1;
	private final int costMismatch = 1;
	private final int costTranspos = 1;
	private Type type;
	
	private boolean isMatch(int i, int j) 
	{
		char col = searchTerm.charAt(i-1);
		char row = searchText.charAt(j-1);
		if (col == row || row == '-')
			return true;
		else
			return false;
	}

	public int distance() {
		
		int[][] matrix = new int[searchTerm.length()+1][searchText.length()+1]; // [row][col]
		
		 // first column: start-gap penalties for searchTerm
		for (int i = 0; i <= (int)searchTerm.length(); i++)
			matrix[i][0] = i*costIndel;
		
		// first row: start-gap penalties for searchText
		if (type == Type.Global)
		{
			for (int j = 1; j <= (int)searchText.length(); j++)
				matrix[0][j] = j*costIndel;
		}
		else if (type == Type.SemiGlobal)
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
		if (type == Type.Global)
		{
			return matrix[searchTerm.length()][searchText.length()];
		}
		else
		{
			int min = Integer.MAX_VALUE;
			for (int j = 1; j <= searchText.length()+1; j++)
			{
				min = Math.min(min, matrix[searchTerm.length()][j-1]);
			}
			return min;
		}
	
	}
	
	private void writeMatrix(int[][] H)
	{
		for (int i = 0; i < H.length; i++)
		{
			for (int j = 0; j < H[0].length; j++)
			{
				System.out.format(" %3d", H[i][j]);
			}
			System.out.println();
		}
	}

	
	public float matchProb()
	{
		//LogUtils.severe("minMatchProb=" +StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB);
		int dist = distance();
		//LogUtils.severe(String.format("DLevDist(%s,%s) = %d\n", searchTerm, searchText, dist));
		if (type == Type.SemiGlobal)
		{
			return 1.0F - ((float)dist / searchTerm.length());
		}
		else
		{
			return 1.0F - ((float)dist / Math.min(searchTerm.length(), searchText.length()));
		}
	}
	
	/*
	public DamerauLevenshteinNonAdjTranspos(final String searchTerm, final String searchText,
			final Type type, final boolean caseSensitive) 
	{
		if (caseSensitive)
		{
			this.searchTerm = searchTerm;
			this.searchText = searchText;
		}
		else
		{
			this.searchTerm = searchTerm.toLowerCase();
			this.searchText= searchText.toLowerCase();
		}
		this.type = type;
	}
	*/
	
	public PseudoDamerauLevenshtein() {
		//LogUtils.severe("minMatchProb=" +StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB);
	}

	public void init(String searchTerm, String searchText,
			boolean subStringMatch, boolean caseSensitive) 
	{
		if (searchTerm == null || searchText == null)
		{
			throw new IllegalArgumentException("Null searchText/searchTerm!");
		}

		if (caseSensitive)
		{
			this.searchTerm = searchTerm;
			this.searchText = searchText;
		}
		else
		{
			this.searchTerm = searchTerm.toLowerCase();
			this.searchText= searchText.toLowerCase();
		}
		this.type = subStringMatch ? Type.SemiGlobal : Type.Global;
	}

	public boolean matches(String searchTerm, String searchText,
			boolean subStringMatch, boolean caseSensitive) 
	{
		init(searchTerm, searchText, subStringMatch, caseSensitive);
		
		return matchProb() > StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB; 
	}

}
