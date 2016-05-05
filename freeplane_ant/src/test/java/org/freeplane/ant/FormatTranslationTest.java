/**
 * FormatTranslationTest.java
 *
 * Copyright (C) 2010,  Volker Boerchers
 *
 * Translator.java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Translator.java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package org.freeplane.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.tools.ant.Project;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class FormatTranslationTest {
	private static final String TRANSLATIONS_SOURCE_DIR = System.getProperty("TRANSLATIONS_SOURCE_DIR");
	private static String unix = "\n";
	private static String mac = "\r";
	private static String win = "\r\n";

	@Test
	public void testComparator() {
		String[] strings = { "a.b = z", "a.b.c= y", "a.b= x", "a.b = x" };
		Arrays.sort(strings, FormatTranslation.KEY_COMPARATOR);
		assertEquals("stable sort, only by key", "a.b = z", strings[0]);
		assertEquals("stable sort, only by key", "a.b= x", strings[1]);
		assertEquals("stable sort, only by key", "a.b = x", strings[2]);
		assertEquals("stable sort, only by key", "a.b.c= y", strings[3]);
	}

	@Test
	public void testCheckForEmptyValues() {
		final String regex = "\\s*(\\[auto\\]|\\[translate me\\])*\\s*";
		assertTrue(" [auto]\r".matches(regex));
		assertTrue("[translate me]\r".matches(regex));
		assertTrue("\r".matches(regex));
		assertTrue("".matches(regex));
		assertFalse(" [nix]\r".matches(regex));
	}

	@Test
	public void testMatchEolStyle() {
		assertTrue(TaskUtils.matchEolStyle("", unix));
		assertTrue(TaskUtils.matchEolStyle("\n", unix));
		assertTrue(TaskUtils.matchEolStyle("\n\n", unix));
		assertFalse(TaskUtils.matchEolStyle("\r", unix));
		assertFalse(TaskUtils.matchEolStyle("\r\n", unix));
		//
		assertTrue(TaskUtils.matchEolStyle("", win));
		assertTrue(TaskUtils.matchEolStyle("\r\n", win));
		assertTrue(TaskUtils.matchEolStyle("\r\n\r\n", win));
		assertFalse(TaskUtils.matchEolStyle("\r", win));
		assertFalse(TaskUtils.matchEolStyle("\n\r", win));
		assertFalse(TaskUtils.matchEolStyle("\n", win));
	}

	@Test
	public void testCheckEolStyleAndReadLines() throws Exception {
		final String input = "one\r\ntwo\n\rthree\\\nthree.one\n\nfour";
		ArrayList<String> resultList = new ArrayList<String>();
		assertFalse("not unique unix", TaskUtils.checkEolStyleAndReadLines(input, resultList, unix));
		assertEquals("a trailing backslash escapes a new line", 6, resultList.size());
		assertFalse("not unique mac", TaskUtils.checkEolStyleAndReadLines(input, resultList, mac));
		assertEquals(6, resultList.size());
		assertFalse("not unique win", TaskUtils.checkEolStyleAndReadLines(input, resultList, win));
		assertEquals(6, resultList.size());
		//
		String unixInput = input.replaceAll("\r\n|\n|\r", unix);
		System.out.println("unixInput='" + f(unixInput) + "'");
		assertTrue("unique unix", TaskUtils.checkEolStyleAndReadLines(unixInput, resultList, unix));
		assertFalse("not mac", TaskUtils.checkEolStyleAndReadLines(unixInput, resultList, mac));
		assertFalse("not win", TaskUtils.checkEolStyleAndReadLines(unixInput, resultList, win));
		assertEquals("a trailing backslash escapes a new line", 6, resultList.size());
		//
		String macInput = input.replaceAll("\r\n|\n|\r", mac);
		System.out.println("macInput='" + f(macInput) + "'");
		assertTrue("unique mac", TaskUtils.checkEolStyleAndReadLines(macInput, resultList, mac));
		assertFalse("not unix", TaskUtils.checkEolStyleAndReadLines(macInput, resultList, unix));
		assertFalse("not win", TaskUtils.checkEolStyleAndReadLines(macInput, resultList, win));
		assertEquals("a trailing backslash escapes a new line", 6, resultList.size());
		//
		String winInput = input.replaceAll("\r\n|\n|\r", win);
		System.out.println("winInput='" + f(winInput) + "'");
		assertTrue("unique win", TaskUtils.checkEolStyleAndReadLines(winInput, resultList, win));
		assertFalse("not unix", TaskUtils.checkEolStyleAndReadLines(winInput, resultList, unix));
		assertFalse("not mac", TaskUtils.checkEolStyleAndReadLines(winInput, resultList, mac));
		assertEquals("a trailing backslash escapes a new line", 6, resultList.size());
		String resource = TaskUtils.readFile(new File(new File("./src/test/resources/unsorted"), "Test_de.properties"));
		assertTrue("not unix", TaskUtils.checkEolStyleAndReadLines(resource, resultList, unix));
	}

	@Test
	public void testRemoveEmptyLines() throws Exception {
		final String msgConserved = "empty lines should be conserved";
		final String msgRemoved = "empty lines should be removed";
		final FormatTranslation formatTranslation = new FormatTranslation();
		String input;
		ArrayList<String> lines = new ArrayList<String>();
		//
		input = "\n \nx=y\n\n";
		assertTrue("unique unix", TaskUtils.checkEolStyleAndReadLines(input, lines, unix));
		assertEquals(msgConserved, 4, lines.size());
		assertEquals(msgRemoved, 1, formatTranslation.processLines("a_file", new ArrayList<String>(lines)).size());
		//
		input = "\n";
		assertTrue("unique unix", TaskUtils.checkEolStyleAndReadLines(input, lines, unix));
		assertEquals(msgConserved, 1, lines.size());
		assertEquals(msgRemoved, 0, formatTranslation.processLines("a_file", new ArrayList<String>(lines)).size());
		//
		input = "  \n";
		assertTrue("unique unix", TaskUtils.checkEolStyleAndReadLines(input, lines, unix));
		assertEquals(msgConserved, 1, lines.size());
		assertEquals(msgRemoved, 0, formatTranslation.processLines("a_file", new ArrayList<String>(lines)).size());
		//
		input = "x=y";
		assertTrue("unique unix", TaskUtils.checkEolStyleAndReadLines(input, lines, unix));
		assertEquals(msgConserved, 1, lines.size());
		assertEquals(msgRemoved, 1, formatTranslation.processLines("a_file", new ArrayList<String>(lines)).size());
		//
	}

	private String f(String input) {
		return input.replace("\n", "\\n").replace("\r", "\\r");
	}

	@Test
	public void testPlaceholderCheck() throws Exception {
		final FormatTranslation formatTranslation = new FormatTranslation();
		String input;
		ArrayList<String> lines = new ArrayList<String>();
		//
		input = "x = a {1} without a 0\n" //
			+ "y = a $1 instead of a {0}";
		// no actual test as long as those tests are not treated as failures
		TaskUtils.checkEolStyleAndReadLines(input, lines, unix);
		formatTranslation.processLines("a_file", new ArrayList<String>(lines));
	}

	@Ignore // this tests causes a lot of modified files...
	@Test
	public void testFormatTranslation() {
		final FormatTranslation formatTranslation = new FormatTranslation();
		final Project project = TaskUtils.createProject(formatTranslation);
		formatTranslation.setTaskName("format-translation");
		formatTranslation.setProject(project);
		formatTranslation.setEolStyle("unix");
		formatTranslation.setDir(new File("./src/test/resources/unsorted"));
		formatTranslation.setIncludes("Test_*.properties");
		formatTranslation.execute();
		System.out.println("done");
	}

	@Test
	public void convertsUnicodeToUpperCase(){
		final FormatTranslation formatTranslation = new FormatTranslation();
		Assert.assertThat(formatTranslation.convertUnicodeCharacterRepresentation("u"), CoreMatchers.equalTo("u"));
		Assert.assertThat(formatTranslation.convertUnicodeCharacterRepresentation("\\Uabcde"), CoreMatchers.equalTo("\\uABCDe"));
		Assert.assertThat(formatTranslation.convertUnicodeCharacterRepresentation("\\uabcde"), CoreMatchers.equalTo("\\uABCDe"));
		Assert.assertThat(formatTranslation.convertUnicodeCharacterRepresentation("1\\Uabcde"), CoreMatchers.equalTo("1\\uABCDe"));
	}

	@Test
	public void convertsLatin1toUnicode() {
		final FormatTranslation formatTranslation = new FormatTranslation();
		Assert.assertThat(formatTranslation.convertUnicodeCharacterRepresentation("ä"),
		    CoreMatchers.equalTo("\\u00E4"));
		Assert.assertThat(formatTranslation.convertUnicodeCharacterRepresentation("ä1"),
		    CoreMatchers.equalTo("\\u00E41"));
	}
}
