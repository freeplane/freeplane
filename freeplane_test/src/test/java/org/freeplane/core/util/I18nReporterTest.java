package org.freeplane.core.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class I18nReporterTest {
	@Test
	public void testGetHeader() {
		I18nReporter reporter = new I18nReporter();
		assertEquals("<tr><th>Translation</th><td>ar</td></tr>", reporter.getHeader("Translation", Arrays
		    .asList(CountryCode.ar)));
	}
	
	@Test
	public void testGetNoEntryMap() {
		I18nReporter r = new I18nReporter();
		r.getSummary();
	}
}
