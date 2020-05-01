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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

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
	private Stack<Alignment> alignmentsInProgress;
	private ArrayList<Alignment> alignmentsDone;
	
	public class Alignment implements Comparable<Alignment>
	{
		private final String searchTermString;
		private final String searchTextString;
		private final double prob;
		private final int matchStart;
		private final int matchEnd;
		private final int r, c;
		
		public int getMatchStart()
		{
			return matchStart;
		}
		
		public int getMatchEnd()
		{
			return matchEnd;
		}
		
		public boolean overlapsWith(final Alignment other)
		{	
			return (matchStart <= other.matchStart && other.matchStart <= matchEnd-1) || // endpoint of this lies in other
				   (other.matchStart <= matchStart && matchStart <= other.matchEnd-1); // endpoint of other lies in this
				   
		}
				
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + c;
			result = prime * result + matchEnd;
			result = prime * result + matchStart;
			long temp;
			temp = Double.doubleToLongBits(prob);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + r;
			result = prime
					* result
					+ ((searchTermString == null) ? 0 : searchTermString
							.hashCode());
			result = prime
					* result
					+ ((searchTextString == null) ? 0 : searchTextString
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Alignment other = (Alignment) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (c != other.c) {
				return false;
			}
			if (matchEnd != other.matchEnd) {
				return false;
			}
			if (matchStart != other.matchStart) {
				return false;
			}
			if (Double.doubleToLongBits(prob) != Double
					.doubleToLongBits(other.prob)) {
				return false;
			}
			if (r != other.r) {
				return false;
			}
			if (searchTermString == null) {
				if (other.searchTermString != null) {
					return false;
				}
			} else if (!searchTermString.equals(other.searchTermString)) {
				return false;
			}
			if (searchTextString == null) {
				if (other.searchTextString != null) {
					return false;
				}
			} else if (!searchTextString.equals(other.searchTextString)) {
				return false;
			}
			return true;
		}



		public String getMatch()
		{
			return searchText.substring(matchStart, matchEnd);
		}
		
		public int compareTo(final Alignment other)
		{
			if (prob == other.prob)
			{
				return new Integer(getMatch().length()).compareTo(new Integer(other.getMatch().length())); 
			}
			else
			{
				return new Double(prob).compareTo(new Double(other.prob));
			}
		}
		
		public void print()
		{
			System.out.format("Alignment@%x[%.2f]:\n%s\n%s\n=> matches '%s' [%d,%d]\n",
					hashCode(), prob, searchTermString, searchTextString, getMatch(),
					matchStart,matchEnd);
		}
		
		@Override
		public String toString()
		{
			return String.format("Ali@%x[%s,%.2f,%d,%d]", hashCode(), getMatch(), prob, matchStart, matchEnd);
		}
		
		public Alignment(final String searchTermString, final String searchTextString, final double prob,
				final int matchStart, final int matchEnd, final int r, final int c)
		{
			this.searchTermString = searchTermString;
			this.searchTextString = searchTextString;
			this.prob = prob;
			this.matchStart = matchStart;
			this.matchEnd = matchEnd;
			this.r = r;
			this.c = c;
		}



		private PseudoDamerauLevenshtein getOuterType() {
			return PseudoDamerauLevenshtein.this;
		}
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
	
	public int distance() {
		
		matrix = new int[searchTerm.length()+1][searchText.length()+1]; // [row][col]
		
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
	
	public List<Alignment> computeAlignments(final double minProb)
	{
		alignmentsInProgress = new Stack<Alignment>();
		alignmentsDone = new ArrayList<Alignment>();
		
		int dist = distance(); // this computes the Dynamic Programming matrix according to Levenshtein
		
		if (type == Type.Global && getMatchProb(dist) > minProb)
		{
			alignmentsInProgress.push(new Alignment("", "", getMatchProb(dist), 0, searchText.length(),
					searchTerm.length(), searchText.length()));
		}
		else
		{
			// semi-global "substring" alignment
			StringBuilder searchTermSuffix = new StringBuilder();
			StringBuilder searchTextSuffix = new StringBuilder();
			for (int c = searchText.length() + 1; c >= 1; c--)
			{
				if (c <= searchText.length())
				{
					searchTermSuffix.append('-');
					searchTextSuffix.insert(0, searchText.charAt(c-1));
				}
				double prob = getMatchProb(matrix[searchTerm.length()][c-1]); 
				if (prob > minProb)
				{
					alignmentsInProgress.push(new Alignment(searchTermSuffix.toString(), searchTextSuffix.toString(),
							prob, 0, searchText.length() - searchTextSuffix.length(), searchTerm.length(), c - 1));
				}
			}
		}
		
		while (!alignmentsInProgress.isEmpty())
		{
			developAlignment(alignmentsInProgress.pop());
		}
		
		// filter (overlapping) alignments
		alignmentsDone = filterAlignments(alignmentsDone);
		
		sortAlignments(alignmentsDone);

		/*
		System.out.format("--NON-OVERLAPPPING ALIGNMENTS-------------------\n");
		for (Alignment ali: alignmentsDone)
		{
			ali.print();
		}
		*/
		
		matrix = null;
		
		//return alignmentsDone.toArray(new Alignment[alignmentsDone.size()]);
		return alignmentsDone;
	}
	
	/**
	 * Keep only non-overlapping matches (alignments) while preferring alignments with high score (prob)
	 * TODO: this is a heuristic, is the problem NP complete?
	 * => probably, see "maximum set packing"
	 * 
	 * @param alignments alignments list to filter
	 * @return filtered alignment list
	 */
	static ArrayList<Alignment> filterAlignments(final ArrayList<Alignment> alignments)
	{
		if (alignments.isEmpty())
			return new ArrayList<Alignment>();
		
		// sort by score and match length (see Alignment.compareTo()) 
		Collections.sort(alignments, Collections.reverseOrder());
		
		ArrayList<Alignment> clusters = new ArrayList<Alignment>(alignments.size());
		// start with a single cluster
		clusters.add(alignments.get(0));
		alignments.remove(0);
		
		// assign alignments to clusters
		for (Alignment ali: alignments)
		{
			boolean found_cluster = false;
			for (int j = 0; j < clusters.size(); j++)
			{
				if (ali.overlapsWith(clusters.get(j)))
				{
					found_cluster = true;
					// keep either current cluster center or set to 'ali'
					if (ali.compareTo(clusters.get(j)) > 0)
					{
						clusters.set(j, ali);
					}
				}
			}
			if (!found_cluster)
			{
				clusters.add(ali);
			}
		}
		return clusters;
	}	
	
	/**
	 * Sort alignments (matches) by start positions
	 * @param alignments list of alignments to sort
	 */
	static void sortAlignments(final ArrayList<Alignment> alignments)
	{
		Collections.sort(alignments, new Comparator<Alignment>()
				{

					public int compare(Alignment o1, Alignment o2) {
						return new Integer(o1.matchStart).compareTo(o2.matchStart);
					}
			
				});
	}
	
//	private void printAlignmentsFrom(final String searchTermSuffix, final String searchTextSuffix, final int r, final int c,
//			double prob, int matchStart, int matchEnd)
	private void developAlignment(final Alignment ali)
	{
		System.out.format("developAlignment(term=%s, text=%s, r=%d, c=%d)",
				ali.searchTermString, ali.searchTextString, ali.r, ali.c);
		
		if (ali.r == 0 && ali.c == 0)
		{
			alignmentsDone.add(ali);
			System.out.println();
			ali.print();
		}
		else
		{
			// TODO: comments!!
			
			// match/mismatch
			if (ali.r >= 1 && ali.c >= 1 && matrix[ali.r][ali.c] == matrix[ali.r-1][ali.c-1] + (isMatch(ali.r,ali.c) ? 0 : costMismatch))
			{
				System.out.format("=> match/mismatch\n");
				
				alignmentsInProgress.push(new Alignment(
						searchTerm.charAt(ali.r-1) + ali.searchTermString,
						searchText.charAt(ali.c-1) + ali.searchTextString,
						ali.prob, ali.matchStart, ali.matchEnd, ali.r - 1, ali.c - 1)
						);
			}

			/*
			// free insertions at the beginning of the searchTerm
			if (ali.c >= 1 && type == Type.SemiGlobal && ali.r == 0 && matrix[ali.r][ali.c-1] == 0)
			{
				System.out.format("=> insertion at beginning\n");
				
				alignmentsInProgress.push(new Alignment(
						"-" + ali.searchTermString,
						searchText.charAt(ali.c-1) + ali.searchTextString,
						ali.prob, ali.matchStart + 1, ali.matchEnd, ali.r, ali.c - 1)
						);
			}
			*/
			if (type == Type.SemiGlobal && ali.r == 0)
			{
				System.out.format("=> insertions at beginning\n");
				int c = ali.c, matchStart = ali.matchStart;
				StringBuilder searchTermPrefix = new StringBuilder();
				StringBuilder searchTextPrefix = new StringBuilder();
				while (c > 0)
				{
					searchTermPrefix.append('-');
					searchTextPrefix.insert(0, searchText.charAt(c-1));
					matchStart += 1;
					c--;
				}
				alignmentsInProgress.push(new Alignment(
						searchTermPrefix.toString() + ali.searchTermString,
						searchTextPrefix.toString() + ali.searchTextString,
						ali.prob, matchStart, ali.matchEnd, 0, 0)
						);
			}

			// insertion
			if (ali.c >= 1 && matrix[ali.r][ali.c] == matrix[ali.r][ali.c-1] + costIndel)
			{
				System.out.format("=> insertion\n");

				alignmentsInProgress.push(new Alignment(
						"-" + ali.searchTermString,
						searchText.charAt(ali.c-1) + ali.searchTextString,
						ali.prob, ali.matchStart, ali.matchEnd, ali.r, ali.c - 1)
						);
			}
						
			// deletion
			if (ali.r >= 1 && matrix[ali.r][ali.c] == matrix[ali.r-1][ali.c] + costIndel)
			{
				System.out.format("=> deletion\n");
				
				alignmentsInProgress.push(new Alignment(
						searchTerm.charAt(ali.r-1) + ali.searchTermString,
						"-" + ali.searchTextString,
						ali.prob, ali.matchStart, ali.matchEnd, ali.r - 1, ali.c)
						);
			}
			
			// Damerau-Extension (transpositions)
			if (ali.r >= 2 && ali.c >= 2 && matrix[ali.r][ali.c] == matrix[ali.r-2][ali.c-2] + costTranspos &&
			    searchTerm.charAt(ali.r-2) == searchText.charAt(ali.c-1) &&
			    searchTerm.charAt(ali.r-1) == searchText.charAt(ali.c-2))
			{
				System.out.format("=> transposition\n");
				
				alignmentsInProgress.push(new Alignment(
						searchTerm.substring(ali.r - 2, ali.r) + ali.searchTermString,
						searchText.substring(ali.c - 2, ali.c) + ali.searchTextString,
						ali.prob, ali.matchStart, ali.matchEnd, ali.r - 2, ali.c - 2)
						);
			}
		}
	}

	private float getMatchProb(final int distance)
	{
		if (type == Type.SemiGlobal)
		{
			return 1.0F - ((float)distance / searchTerm.length());
		}
		else
		{
			return 1.0F - ((float)distance / Math.min(searchTerm.length(), searchText.length()));
		}
	}
	
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

	public void init(String searchTerm, String searchText,
			boolean subStringMatch) 
	{
		if (searchTerm == null || searchText == null)
		{
			throw new IllegalArgumentException("Null searchText/searchTerm!");
		}

        this.searchTerm = searchTerm;
        this.searchText = searchText;
 		this.type = subStringMatch ? Type.SemiGlobal : Type.Global;
	}

	public boolean matches(String searchTerm, String searchText,
			boolean subStringMatch) 
	{
		init(searchTerm, searchText, subStringMatch);
		
		return matchProb() > StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB; 
	}

	/*
	public Match[] getMatches(String searchTerm,
			String searchText, boolean subStringMatch, boolean caseSensitive, double minProb) {
		init(searchTerm, searchText, subStringMatch, caseSensitive);
		List<Match> matches = new ArrayList<Match>();
		for (Alignment ali: computeAlignments(minProb))
		{
			matches.add(new Match(ali.matchStart, ali.matchEnd));
		}
		return matches.toArray(new Match[matches.size()]);
	}
	*/
}
