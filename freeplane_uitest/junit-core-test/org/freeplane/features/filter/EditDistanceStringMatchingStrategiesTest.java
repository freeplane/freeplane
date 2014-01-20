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

import java.util.Collection;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class EditDistanceStringMatchingStrategiesTest {
	
	private EditDistanceStringMatchingStrategy strategy;
	
	public EditDistanceStringMatchingStrategiesTest(EditDistanceStringMatchingStrategy strategy)
	{ 
		this.strategy = strategy;
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		   Object[][] data = new Object[][] { { new DamerauLevenshtein() }, { new PseudoDamerauLevenshtein() } };
		   return Arrays.asList(data);
	}
	
	private void assertDistance(final EditDistanceStringMatchingStrategy strategy,
			final String searchTerm,
			final String searchText,
			final boolean subStringMatch,
			final boolean caseSensitive,
			final int expectedDistance)
	{
		strategy.init(searchTerm, searchText, subStringMatch, caseSensitive);
		int dist = strategy.distance();
		float prob = strategy.matchProb();
		System.out.format("%s(%s,%s) = %d (prob=%.2f) [%s]\n",
				subStringMatch ? "semi-global-dist" : "dist",
				searchTerm, searchText, dist, prob, strategy.getClass());
		Assert.assertEquals(expectedDistance, dist);
	}
	
	
	
	@Test
	public void testSimple()
	{
		assertDistance(strategy, "ABC", "DEF", false, true, 3); // three subst
		assertDistance(strategy, "ACD", "ADE", false, true, 2); // one ins, one del
		assertDistance(strategy, "abcd", "ab", false, true, 2); // two subst
		assertDistance(strategy, "abcd", "cd", false, true, 2); // two subst
		assertDistance(strategy, "flies", "time flies", false, true, 5); // 5 subst
		assertDistance(strategy, "visible", "public Y getXXX() / public void setXXX(...) in camel case", true, true, 5);
		assertDistance(strategy, "hello world", "is the world", true, true, 3);
		assertDistance(strategy, "print", "pointer", true, true, 1);
		assertDistance(strategy, "network", "netzwerk", true, true, 2);
		assertDistance(strategy, "approximately", "aproxximatelly", true, true, 3);
		assertDistance(strategy, "variable", "visible", true, true, 3);
		assertDistance(strategy, "featured article", "featured main article", true, true, 5);
		assertDistance(strategy, "expected distance", "expected edit distance", true, true, 5);
		assertDistance(strategy, "RandomFileAccess", "RandomAccessFolder", false, true, 10);
		assertDistance(strategy, "fitness studio", "fitness center", true, true, 6);
	}

	@Test
	public void testTransposition()
	{
		assertDistance(strategy, "AB", "BA", false, true, 1); // 1 transpos
		assertDistance(strategy, "files", "flies", false, true, 1); // 1 transpos
		
		if (strategy instanceof DamerauLevenshtein)
		{
			// this algo can edit a substring more than once!
			// (see http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance#Algorithm)
			// CA -> AC -> ABC
			assertDistance(strategy, "CA", "ABC", false, true, 2);
		}
		else if (strategy instanceof PseudoDamerauLevenshtein)
		{
			// as opposed to DamerauLevenshtein, this algo cannot edit a substring more than once!
			// => CA -> A -> AB -> ABC
			assertDistance(strategy, "CA", "ABC", false, true, 3);
		}
	}
	
	@Test
	public void testCase()
	{
		assertDistance(strategy, "JOBS", "Jobs", false, false, 0);
		assertDistance(strategy, "JOBS", "Jobs", false, true, 3);
		assertDistance(strategy, "äöüß", "ÄÖÜß", false, false, 0);
		assertDistance(strategy, "äöüß", "ÄÖÜß", false, true, 3);
	}
	
	@Test
	public void testSemiGlobal()
	{
		assertDistance(strategy, "file", "fileb", true, true, 0);
		assertDistance(strategy, "file", "afile", true, true, 0);
		assertDistance(strategy, "abcd", "xefgh", true, true, 4);
		assertDistance(strategy, "files", "time flies by", true, true, 1);
		assertDistance(strategy, "files", "a file is read", true, true, 1);
		assertDistance(strategy, "Number_Format", "new NumberFormat(bla bla bla)", true, true, 1);
		assertDistance(strategy, "files", "time flies by", true, true, 1);
		assertDistance(strategy, "hobbies", "hobbys", true, true, 2);
		assertDistance(strategy, "hobbys", "hobbies", true, true, 2);
	}
}
