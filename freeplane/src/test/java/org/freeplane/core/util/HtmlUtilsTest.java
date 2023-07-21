package org.freeplane.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlUtilsTest {

	@Test
	public void testHtmlToPlain_shouldRemoveTrailingWhitespaces() {
		String input = "<html>\n" +
				"  <head>\n" +
				"    \n" +
				"  </head>\n" +
				"  <body>\n" +
				"    <p>\n" +
				"      A paragraph followed by an empty one\n" +
				"    </p>\n" +
				"    <p>\n" +
				"      \n" +
				"    </p>\n" +
				"  </body>\n" +
				"</html>";
		String expected = "A paragraph followed by an empty one";
		String actual = HtmlUtils.htmlToPlain(input, true, true);
		assertEquals(expected, actual);
	}

	@Test
	public void testHtmlToPlain_shouldRetainTrailingNonBreakingSpaces() {
		String input = "<html>\n" +
				"  <head>\n" +
				"    \n" +
				"  </head>\n" +
				"  <body>\n" +
				"    <p>\n" +
				"      Zero\n" +
				"    </p>\n" +
				"    <p>\n" +
				"      One&nbsp; \n" +
				"    </p>\n" +
				"    <p>\n" +
				"      Two&nbsp;&nbsp; \n" +
				"    </p>\n" +
				"    <p>\n" +
				"      Three&nbsp;&nbsp;&nbsp; \n" +
				"    </p>\n" +
				"    <p>\n" +
				"      EOF\n" +
				"    </p>\n" +
				"  </body>\n" +
				"</html>\n";
		String expected = "Zero\n" +
				"One \n" +
				"Two  \n" +
				"Three   \n" +
				"EOF";
		String actual = HtmlUtils.htmlToPlain(input, true, true);
		assertEquals(expected, actual);
	}
}