package org.freeplane.features.filter;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.freeplane.features.filter.PseudoDamerauLevenshtein.Alignment;
import org.junit.Before;
import org.junit.Test;

public class PseudoDamerauLevenshteinTest {
	
	private PseudoDamerauLevenshtein PDL;
	private ArrayList<PseudoDamerauLevenshtein.Alignment> alignments;

	@Before
	public void setUp()
	{
		PDL = new PseudoDamerauLevenshtein();
		//PDL.init("hobbys", "hobbies", true, true);
		alignments = new ArrayList<PseudoDamerauLevenshtein.Alignment>();
	}
	
	// TODO: make minprob for developAlignments() configurable!
	
	@Test
	public void testSimpleFilterAlignments()
	{
		String searchTerm = "hobbys";
		String searchText = "hobbies";
		PDL.init(searchTerm, searchText, true, true);
		alignments.clear();
		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment(searchTerm, searchText, 1.0, 0, 5, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment(searchTerm, searchText, 0.5, 0, 5, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(1, alignments.size());
		Assert.assertEquals(ali1, alignments.get(0));
	}

	@Test
	public void testSimpleFilterAlignments2()
	{
		String searchTerm = "hobbys";
		String searchText = "hobbies";
		PDL.init(searchTerm, searchText, true, true);
		alignments.clear();
		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment(searchTerm, searchText, 1.0, 0, 7, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment(searchTerm, searchText, 1.0, 0, 7, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(1, alignments.size());
		Assert.assertTrue(alignments.get(0) == ali1 ^ alignments.get(0) == ali2);
	}

	@Test
	//filterAlignments([Ali@1ff7a1e[hobb,0,67,0,4], Ali@1aa57fb[hobbi,0,67,0,5], Ali@763f5d[hobbi,0,67,0,5],
	// Ali@13a317a[hobbie,0,67,0,6], Ali@14a8cd1[hobbies,0,67,0,7], Ali@1630ab9[hobbies,0,67,0,7]])
	public void testSimpleFilterAlignments3()
	{
		String searchTerm = "hobbys";
		String searchText = "hobbies";
		PDL.init(searchTerm, searchText, true, true);
		alignments.clear();
		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment(searchTerm, searchText, 0.67, 0, 4, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment(searchTerm, searchText, 0.67, 0, 5, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali3 = PDL.new Alignment(searchTerm, searchText, 0.67, 0, 5, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali4 = PDL.new Alignment(searchTerm, searchText, 0.67, 0, 6, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali5 = PDL.new Alignment(searchTerm, searchText, 0.67, 0, 7, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali6 = PDL.new Alignment(searchTerm, searchText, 0.67, 0, 7, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
		alignments.add(ali3);
		alignments.add(ali4);
		alignments.add(ali5);
		alignments.add(ali6);
		
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(1,alignments.size());
		Assert.assertTrue(alignments.get(0) == ali5 ^ alignments.get(0) == ali6);
	}
	
	@Test
	public void testSimpleFilterAlignments4()
	{
		String searchTerm = "fit";
		String searchText = "xfityfitz";
		PDL.init(searchTerm, searchText, true, true);
		alignments.clear();
		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment(searchTerm, searchText, 0.67, 1, 3, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment(searchTerm, searchText, 0.67, 1, 5, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali3 = PDL.new Alignment(searchTerm, searchText, 1.0, 0, 7, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
		alignments.add(ali3);
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(1, alignments.size());
		Assert.assertTrue(alignments.get(0) == ali3);
	}
	
	//filterAlignments-unique([Ali@208aef23[fitf,0,67,1,5], Ali@8035a329[fi,0,67,1,3], Ali@28d2570b[fitz,0,67,4,8],
	// Ali@3820e7f9[fit,1,00,1,4], Ali@40684fe1[fit,1,00,4,7], Ali@5ab613ed[fi,0,67,4,6]])
	@Test
	public void testSimpleFilterAlignments5()
	{
		String searchTerm = "fit";
		String searchText = "xfitfitz";
		PDL.init(searchTerm, searchText, true, true);
		alignments.clear();
		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment(searchTerm, searchText, 0.67, 1, 5, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment(searchTerm, searchText, 0.67, 1, 3, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali3 = PDL.new Alignment(searchTerm, searchText, 0.67, 4, 8, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali4 = PDL.new Alignment(searchTerm, searchText, 1.0,  1, 4, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali5 = PDL.new Alignment(searchTerm, searchText, 1.0,  4, 7, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali6 = PDL.new Alignment(searchTerm, searchText, 0.67, 4, 6, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
		alignments.add(ali3);
		alignments.add(ali4);
		alignments.add(ali5);
		alignments.add(ali6);
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(2, alignments.size());
		Assert.assertTrue(alignments.contains(ali4));
		Assert.assertTrue(alignments.contains(ali5));
	}

	@Test
	public void testSimpleFilterAlignments6()
	{
		String searchTerm = "refugee";
		String searchText = "refuge x y";
		PDL.init(searchTerm, searchText, true, true);
		alignments.clear();
		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment(searchTerm, "refug-e", 0.86, 0, 6, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment(searchTerm, "refuge ", 0.86, 0, 7, 0, 0);
//		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment(searchTerm, searchText, 0.71, 0, 5, 0, 0);
//		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment(searchTerm, searchText, 0.86, 0, 6, 0, 0);
//		PseudoDamerauLevenshtein.Alignment ali3 = PDL.new Alignment(searchTerm, searchText, 0.86, 0, 6, 0, 0);
//		PseudoDamerauLevenshtein.Alignment ali4 = PDL.new Alignment(searchTerm, searchText, 0.86, 0, 7, 0, 0);
//		PseudoDamerauLevenshtein.Alignment ali5 = PDL.new Alignment(searchTerm, searchText, 0.71, 0, 8, 0, 0);
//		PseudoDamerauLevenshtein.Alignment ali6 = PDL.new Alignment(searchTerm, searchText, 0.71, 0, 8, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
//		alignments.add(ali3);
//		alignments.add(ali4);
//		alignments.add(ali5);
//		alignments.add(ali6);
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(1, alignments.size());
		Assert.assertTrue(alignments.contains(ali1) ^ alignments.contains(ali2));
	}

	@Test
	public void testSimpleFilterAlignments7()
	{
		String searchTerm = "thee";
		String searchText = "The x y the";
		PDL.init(searchTerm, searchText, true, true);
		alignments.clear();
		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment(searchTerm, "the ", 0.75, 0, 4, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment(searchTerm, "the-", 0.75, 8, 11, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali3 = PDL.new Alignment(searchTerm, "th-e", 0.75, 8, 11, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
		alignments.add(ali3);
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(2, alignments.size());
		Assert.assertTrue(alignments.contains(ali1));
		Assert.assertTrue(alignments.contains(ali2) ^ alignments.contains(ali3));
	}

	@Test
	public void testSimpleFilterAlignments8()
	{
		String searchTerm = "jdsfaskd";
		String searchText = "jsdaljasdf";
		PDL.init(searchTerm, searchText, true, true);
		alignments.clear();
		PseudoDamerauLevenshtein.Alignment ali1 = PDL.new Alignment("", "", 1.0, 2, 4, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali2 = PDL.new Alignment("", "", 0.8, 3, 5, 0, 0);
		PseudoDamerauLevenshtein.Alignment ali3 = PDL.new Alignment("", "", 0.9, 4, 6, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
		alignments.add(ali3);
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(2, alignments.size());
		Assert.assertTrue(alignments.contains(ali1) && alignments.contains(ali3));
		
		alignments.clear();
		ali1 = PDL.new Alignment("", "", 0.8, 2, 4, 0, 0);
		ali2 = PDL.new Alignment("", "", 0.9, 3, 5, 0, 0);
		ali3 = PDL.new Alignment("", "", 0.9, 4, 6, 0, 0);
		alignments.add(ali1);
		alignments.add(ali2);
		alignments.add(ali3);
		alignments = PseudoDamerauLevenshtein.filterAlignments(alignments);
		Assert.assertEquals(1, alignments.size());
		Assert.assertTrue(alignments.contains(ali2));

	}

	@Test
	public void testOverlapsWith()
	{
		// 0. [1,4] [1,4] => true
		Assert.assertTrue(PDL.new Alignment("", "", 0.0, 1, 4, 0, 0)
		  .overlapsWith(PDL.new Alignment("", "", 0.0, 1, 4, 0, 0)));
		
		// 1. [1,2] [2,4] => false
		Assert.assertFalse(PDL.new Alignment("", "", 0.0, 1, 2, 0, 0)
		  .overlapsWith(PDL.new Alignment("", "", 0.0, 2, 4, 0, 0)));
		
		// 2. [1,3] [2,5] => true
		Assert.assertTrue(PDL.new Alignment("", "", 0.0, 1, 3, 0, 0)
		  .overlapsWith(PDL.new Alignment("", "", 0.0, 2, 5, 0, 0)));
		
		// 3. [1,5] [0,6] => true
		Assert.assertTrue(PDL.new Alignment("", "", 0.0, 1, 5, 0, 0)
		  .overlapsWith(PDL.new Alignment("", "", 0.0, 0, 6, 0, 0)));
		
		// 4. [2,4] [1,2] => false
		Assert.assertFalse(PDL.new Alignment("", "", 0.0, 2, 4, 0, 0)
		  .overlapsWith(PDL.new Alignment("", "", 0.0, 1, 2, 0, 0)));
		
		// 5. [2,5] [1,3] => true
		Assert.assertTrue(PDL.new Alignment("", "", 0.0, 2, 5, 0, 0)
		  .overlapsWith(PDL.new Alignment("", "", 0.0, 1, 3, 0, 0)));
	}
	
	@Test
	public void testLongAlignment1()
	{
		PDL.init("thee", "The x y the", true, false); //  alfdsj the aflsjd thex jlsadf thee.
		List<PseudoDamerauLevenshtein.Alignment> alis = PDL.computeAlignments(0.65);
		System.out.format("-------testLongAlignment1() final alignments:\n\n");
		for (Alignment ali: alis)
		{
			ali.print();
			for (Alignment ali2: alis)
			{
				if (ali == ali2)
					continue;
				Assert.assertFalse(ali.overlapsWith(ali2));
			}
		}
		System.out.format("-------END OF testLongAlignment1() final alignments:\n\n");
	}

	@Test
	public void testSimpleAlignments()
	{
		// TODO: special test for alignments!
		System.out.format("\n\n-------testSimpleAlignments() ------------------------\n");
		PseudoDamerauLevenshtein DL = new PseudoDamerauLevenshtein();
		//DL.init("AB", "CD", false, true);
		//DL.init("ACD", "ADE", false, true);
		//DL.init("AB", "XAB", false, true);
		//DL.init("AB", "XAB", true, true);
		//DL.init("fit", "xfity", true, true);
		//DL.init("fit", "xxfityyy", true, true);
		//DL.init("ABCD", "BACD", false, true);
		//DL.init("fit", "xfityfitz", true, true);
		//DL.init("fit", "xfitfitz", true, true);
		//DL.init("fit", "xfitfitfitfitz", true, true);
		//DL.init("setup", "set up", true, true);
		//DL.init("set up", "setup", true, true);
		//DL.init("hobbies", "hobbys", true, true);
		//DL.init("hobbys", "hobbies", true, true);
		//DL.init("thee", "The x y the jdlsjds salds", true, false);
		DL.init("Bismark", "... Bismarck lived...Bismarck reigned...", true, true);
		//DL.init("refugee", "refuge x y", true, true);
		//StringMatchingStrategy.APPROXIMATE_MATCHING_MINPROB
		List<PseudoDamerauLevenshtein.Alignment> alis = DL.computeAlignments(0.65);
		System.out.format("----------result of testSimpleAlignments() ---------------------\n\n");
		for (Alignment ali: alis)
		{
			ali.print();
		}
	}
}
