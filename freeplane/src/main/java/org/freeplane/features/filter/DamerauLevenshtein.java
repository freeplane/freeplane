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
 * Damerau-Levenshtein implementation, computes the edit distance (ins/del/subst/transpos)
 * between a search term and a text to search against.
 * see http://en.wikipedia.org/wiki/Damerau–Levenshtein_distance
 * The basic algorithm is originally from Wikipedia, and was extended for semi-global alignments.
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
public class DamerauLevenshtein implements EditDistanceStringMatchingStrategy {

	private String searchTerm;
	private String searchText;
	private Type type;
	private int alphabetLength;

	public int distance()
	{
		final int INFINITY = searchTerm.length() + searchText.length();
		int[][] H = new int[searchTerm.length()+2][searchText.length()+2];
		H[0][0] = INFINITY;
		for(int i = 0; i<=searchTerm.length(); i++) {
			H[i+1][1] = i;
			H[i+1][0] = INFINITY;
		}
		for(int j = 0; j<=searchText.length(); j++) {
			H[1][j+1] = (type == Type.Global) ? j : 0;
			H[0][j+1] = INFINITY;
		}
		int[] DA = new int[alphabetLength];
		Arrays.fill(DA, 0);
		for(int i = 1; i<=searchTerm.length(); i++) {
			int DB = 0;
			for(int j = 1; j<=searchText.length(); j++) {
				int i1 = DA[searchText.charAt(j-1)];
				int j1 = DB;
				int d = ((searchTerm.charAt(i-1)==searchText.charAt(j-1))?0:1);
				if(d==0) DB = j;
				H[i+1][j+1] =
						min(H[i][j]+d,
								H[i+1][j] + 1,
								H[i][j+1]+1,
								H[i1][j1] + (i-i1-1) + 1 + (j-j1-1));
			}
			DA[searchTerm.charAt(i-1)] = i;
		}
		//writeMatrix(H);
		if (type == Type.Global)
		{
			return H[searchTerm.length()+1][searchText.length()+1];
		}
		else
		{
			int min = Integer.MAX_VALUE;
			for (int j = 1; j <= searchText.length() + 1; j++)
			{
				min = Math.min(min, H[searchTerm.length()+1][j]);
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

	private static int min(int ... nums)
	{
		int min = Integer.MAX_VALUE;
		for (int num : nums) {
			min = Math.min(min, num);
		}
		return min;
	}

	public float matchProb()
	{
		if (type == Type.SemiGlobal)
		{
			return 1.0F - ((float)distance() / searchTerm.length());
		}
		else
		{
			return 1.0F - ((float)distance() / Math.min(searchTerm.length(), searchText.length()));
		}
	}

	/*
	public DamerauLevenshtein(final String searchTerm, final String searchText,
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
		int maxCodePoint = 0;
		for (int i = 0; i < searchTerm.length(); i++)
		{
			maxCodePoint = Math.max(maxCodePoint, searchTerm.charAt(i));
		}
		for (int i = 0; i < searchText.length(); i++)
		{
			maxCodePoint = Math.max(maxCodePoint, searchText.charAt(i));
		}
		alphabetLength = maxCodePoint + 1;
	}
	*/

	public DamerauLevenshtein() {

	}

	public void init(final String searchTerm, final String searchText, final boolean subStringMatch,
			final boolean caseSensitive)
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
		int maxCodePoint = 0;
		for (int i = 0; i < this.searchTerm.length(); i++)
		{
			maxCodePoint = Math.max(maxCodePoint, this.searchTerm.charAt(i));
		}
		for (int i = 0; i < this.searchText.length(); i++)
		{
			maxCodePoint = Math.max(maxCodePoint, this.searchText.charAt(i));
		}
		alphabetLength = maxCodePoint + 1;
	}

	public boolean matches(final String searchTerm, final String searchText, final boolean subStringMatch,
			final boolean caseSensitive)
	{
		//LogUtils.severe(String.format("DL(%s,%s)\n", searchTerm, searchText));
		init(searchTerm, searchText, subStringMatch, caseSensitive);

		return matchProb() > StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB;
	}
}
