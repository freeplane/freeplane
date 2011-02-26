package org.freeplane.features.common.format;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.util.HtmlUtils;
import org.junit.Test;

public class PatternFormatTest {
	private static final String HTML_BODY_INDENT = "    ";
	private static final ArrayList<String> singlePatterns = toList("e = %+10.4f" //
	    , "Amount gained or lost since last statement: $ %(,.2f" //
	    , "Local time: %tT" //
	);
	private static final ArrayList<String> multiPatterns = toList("%tH:%tM" //
	    , "%tH:%tM:%tS" //
	    , "%tI:%tM:%tS %Tp" //
	    , "%tm/%td/%ty" //
	    , "%tY-%tm-%td" //
	    , "%ta %tb %td %tT %tZ %tY" //
	// , "Duke's Birthday: %1$tm %1$te,%1$tY" // in principle this could work
	// , "Duke's Birthday: %1$tm %<$te,%<$tY" // in principle this could work
	);
	private static final ArrayList<String> parameterizedPatterns = toList("%3$2s" //
	        + "%2$2s" //
	        + "%1$2s" //
	, "Unable to open file '%1$s':" //
	    , "Duke's Birthday: %1$tm" //
	    , "Duke's Birthday: %1$te" //
	    , "Duke's Birthday: %1$tY" //
	);
	static {
		for (String pattern : multiPatterns) {
			final String[] fragments = pattern.split("(?=%)");
			for (String string : fragments) {
				if (string.contains("%"))
					singlePatterns.add(string);
			}
		}
	}

	@Test
	public void testMatchPattern() {
		final Pattern pattern = PatternFormat.formatterPattern;
		for (String string : singlePatterns) {
			assertMatches(pattern, string);
		}
	}

	private static ArrayList<String> toList(String... strings) {
		return new ArrayList<String>(Arrays.asList(strings));
	}

	@Test
	public void test_guessPatternFormat() {
		for (String pattern : singlePatterns) {
			assertNotNull("should be a formatter pattern: " + pattern, PatternFormat.guessPatternFormat(pattern));
		}
		for (String pattern : multiPatterns) {
			assertNull("only single pattern may be accepted as a formatter pattern: " + pattern,
			    PatternFormat.guessPatternFormat(pattern));
		}
		for (String pattern : parameterizedPatterns) {
			assertNull("positional parameters (n$) are not accepted as a formatter patterns: " + pattern,
			    PatternFormat.guessPatternFormat(pattern));
		}
	}

	@Test
	public void testFormat() {
		for (String pattern : singlePatterns) {
			testOnePattern(pattern);
		}
	}

	private void testOnePattern(String pattern) {
		final PatternFormat formatter = PatternFormat.guessPatternFormat(pattern);
		assertNotNull("could not create a formatter for pattern " + pattern, formatter);
		if (formatter.acceptsDate())
			System.err.println(pattern + "->" + formatter.format(new Date()));
		else if (formatter.acceptsNumber())
			System.err.println(pattern + "->" + formatter.format(1223.456789));
		else
			System.err.println(pattern + "->" + formatter.format("Hello world!"));
	}

	private void assertMatches(final Pattern pattern, String string) {
		final Matcher m = pattern.matcher(string);
		final boolean matches = m.find();
		if (matches)
			System.out.println("match " + string + "->" + m.group() + "->" + m.group(1));
		else
			System.out.println("no match for " + string);
		assertTrue(string, matches);
	}

	@Test
	public void testTransform() {
		final long t = System.currentTimeMillis();
		for (int i = 0; i < 20000; ++i) {
			String text = makeHtmlText("one\ntwo" + i);
			assertEquals("one\n" + HTML_BODY_INDENT + "two" + i, HtmlUtils.extractRawBody(text));
		}
		System.err.println((System.currentTimeMillis() - t) / 1000.);
	}

	private String makeHtmlText(String body) {
		return "<html>" //
		        + "  <head>" //
		        + HTML_BODY_INDENT + "  </head>" //
		        + "  <body>" //
		        + HTML_BODY_INDENT + body.replace("\n", "\n" + HTML_BODY_INDENT) //
		        + "  </body>" //
		        + "</html>";
	}

	final static Pattern bodyPattern = Pattern.compile("<body>\\s*(.*?)\\s*</body>", Pattern.DOTALL);

	// alternative implementation of HtmlUtils.extractRawBody() - approximately two times slower
	public static String extractRawBodyRegexp(final String text) {
		final Matcher matcher = bodyPattern.matcher(text);
		if (matcher.find())
			return matcher.group(1);
		return "";
	}

	public void performanceTest() {
		final int count = 20000;
		final long t = System.currentTimeMillis();
		for (int i = 0; i < count; ++i) {
			String text = makeHtmlText("one\ntwo" + i);
			//			assertEquals("one\n" + HTML_BODY_INDENT + "two" + i, HtmlUtils.extractRawBody(text));
			assertEquals("one\n" + HTML_BODY_INDENT + "two" + i, extractRawBodyRegexp(text));
		}
		System.err.println((System.currentTimeMillis() - t) / 1000.);
	}
}
