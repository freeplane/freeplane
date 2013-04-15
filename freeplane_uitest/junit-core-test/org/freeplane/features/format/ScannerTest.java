/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
package org.freeplane.features.format;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.freeplane.main.application.FreeplaneGUIStarter;
import org.freeplane.main.headlessmode.FreeplaneHeadlessStarter;
import org.freeplane.main.headlessmode.HeadlessMModeControllerFactory;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Volker Boerchers
 */
public class ScannerTest {
	private static final String TYPE_DATE = IFormattedObject.TYPE_DATE;
	private static final String TYPE_DATETIME = IFormattedObject.TYPE_DATETIME;
	private static ScannerController scannerController;
	private static String datePattern;
	private static String datetimePattern;
	private static FormatController formatController;

	@BeforeClass
	public static void initStatics() throws Exception {
		// FIXME: we have to start Freeplane to create a Controller for script execution
		System.setProperty("org.freeplane.nosplash", "true");
		new FreeplaneHeadlessStarter().createController();
		HeadlessMModeControllerFactory.createModeController();
//		new FreeplaneGUIStarter().createController();
//		MModeControllerFactory.createModeController();
		formatController = FormatController.getController();
		scannerController = ScannerController.getController();
		datePattern = ((SimpleDateFormat) formatController.getDefaultFormat(TYPE_DATE)).toPattern();
		datetimePattern = ((SimpleDateFormat) formatController.getDefaultFormat(TYPE_DATETIME)).toPattern();
	}

	@Test
	public void testNumberLiterals() throws Exception {
		scannerController.selectScanner(new Locale("en"));
		ScannerController parser = scannerController;
		// null safe
		assertFormattedNumberEquals(null, parser.parse(null));
		// integer
		assertFormattedNumberEquals(0., parser.parse("0"));
		assertFormattedNumberEquals(-1., parser.parse("-1"));
		assertFormattedNumberEquals(999., parser.parse("999"));
		// floats
		assertFormattedNumberEquals(0.12345, parser.parse("0.12345"));
		assertFormattedNumberEquals(-1.12345, parser.parse("-1.12345"));
		assertFormattedNumberEquals(999.12345, parser.parse("999.12345"));
		// leading decimal separator
		assertFormattedNumberEquals(.12345, parser.parse(".12345"));
		assertFormattedNumberEquals(-0.12345, parser.parse("-.12345"));
		// invalid chars
		assertParseFailure("parser should not skip invalid chars", parser, "999x.12345");
		assertParseFailure("parser should not skip invalid chars", parser, "x0.12345");
		assertParseFailure("parser should not skip invalid chars", parser, "-1.12345x");
		// whitespace
		assertParseFailure("parser should not ignore whitespace", parser, "999 .12345");
		assertParseFailure("parser should not ignore whitespace", parser, "-1.12345 ");
		assertParseFailure("parser should not ignore whitespace", parser, " 0.12345");
	}

	private void assertParseFailure(String message, ScannerController parser, String input) {
		assertEquals(message, input, parser.parse(input));
	}

	private void testDecimalParserImpl(final Locale locale, final char sep, final char thousand_sep) {
		scannerController.selectScanner(locale);
		ScannerController parser = scannerController;
		// null safe
		assertFormattedNumberEquals(null, parser.parse(null));
		// integer
		assertFormattedNumberEquals(0., parser.parse("0"));
		assertFormattedNumberEquals(-1., parser.parse("-1"));
		assertFormattedNumberEquals(999., parser.parse("999"));
		// floats
		assertFormattedNumberEquals("decimal separator is the comma", 0.12345, parser.parse("0" + sep + "12345"));
		assertFormattedNumberEquals("decimal separator is the comma", -1.12345, parser.parse("-1" + sep + "12345"));
		assertFormattedNumberEquals("decimal separator is the comma", 999.12345, parser.parse("999" + sep + "12345"));
		// leading decimal separator
		assertFormattedNumberEquals(.12345, parser.parse("" + sep + "12345"));
		assertFormattedNumberEquals(-0.12345, parser.parse("-" + sep + "12345"));
		// invalid symbols
		assertFormattedNumberEquals("thousand sep. not recognized", 99999., parser.parse("99" + thousand_sep + "999"));
		assertFormattedNumberEquals("thousand sep. not recognized", 999999.99,
		    parser.parse("999" + thousand_sep + "999" + sep + "99"));
		// invalid chars
		assertParseFailure("parser should not skip invalid chars", parser, "999x" + sep + "12345");
		assertParseFailure("parser should not skip invalid chars", parser, "x0" + sep + "12345");
		assertParseFailure("parser should not skip invalid chars", parser, "-1" + sep + "12345x");
		// whitespace
		assertParseFailure("parser should not ignore whitespace", parser, "999 " + sep + "12345");
		assertParseFailure("parser should not ignore whitespace", parser, "-1" + sep + "12345 ");
		assertParseFailure("parser should not ignore whitespace", parser, " 0" + sep + "12345");
	}

	@Test
	public void testDecimalParser_en() throws Exception {
		final Locale locale = new Locale("en");
		final char sep = '.';
		final char thousand_sep = ',';
		testDecimalParserImpl(locale, sep, thousand_sep);
	}

	@Test
	public void testDecimalParser_de() throws Exception {
		final Locale locale = new Locale("de");
		final char sep = ',';
		final char thousand_sep = '.';
		testDecimalParserImpl(locale, sep, thousand_sep);
	}

	private void assertFormattedNumberEquals(Double expected, Object actual) {
		assertIsFormattedNumber(null, actual);
		if (expected != actual)
			assertEquals(expected, toDouble(actual), 1e-7);
	}

	private void assertFormattedNumberEquals(String message, Double expected, Object actual) {
		assertIsFormattedNumber(message, actual);
		if (expected != actual)
			assertEquals(message, expected, toDouble(actual), 1e-7);
	}

	private void assertIsFormattedNumber(String message, Object actual) {
		if (actual != null) {
			message = message == null ? "" : message + ": ";
			assertEquals(message + "not a FormattedNumber: " + actual, FormattedNumber.class, actual.getClass());
		}
	}

	// Parsers return formatted objects -> unwrap them
	private Double toDouble(Object parseResult) {
		if (parseResult == null)
			return null;
		final FormattedNumber formattedNumber = (FormattedNumber) parseResult;
		return formattedNumber.doubleValue();
	}

	@Test
	public void testIsoDateParser() throws Exception {
		testIsoDateParserImpl(new Locale("en"));
		testIsoDateParserImpl(new Locale("de"));
	}

	private void testIsoDateParserImpl(Locale locale) {
		scannerController.selectScanner(locale);
		ScannerController parser = scannerController;
		// null safe
		assertFormattedDateEquals(null, parser.parse(null), datePattern);
		assertFormattedDateEquals("2010-08-16 22:31:55.123+0530", parser.parse("2010-08-16 22:31:55.123+0530"),
		    datetimePattern);
		// note that SimpleDateFormat uses local time zone for conversion - use DateFormatUtils instead!
		final String Z = getLocalTimeZoneOffsetString();
		assertFormattedDateEquals(("2010-08-16 22:31:55.123" + Z), parser.parse("2010-08-16 22:31:55.123"),
		    datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("2010-08-16 22:31:55.000"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("2010-08-16 22:31:55"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:00", parser.parse("2010-08-16 22:31"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 00:00:00", parser.parse("2010-08-16"), datePattern);
		//
		assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("2010-08-16T22:31:55.000"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("2010-08-16T22:31:55"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:00", parser.parse("2010-08-16T22:31"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 00:00:00", parser.parse("2010-08-16"), datePattern);
		// ambiguous: not a date but a number:
		// assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("20100816223155.000"), datetimePattern);
		// assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("20100816223155"), datetimePattern);
		// assertFormattedDateEquals("2010-08-16 22:31:00", parser.parse("201008162231"), datetimePattern);
		// assertFormattedDateEquals("2010-08-16 00:00:00", parser.parse("20100816"), datePattern);
		// assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("20100816T223155.000"), datetimePattern);
		// assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("20100816T223155"), datetimePattern);
		// assertFormattedDateEquals("2010-08-16 22:31:00", parser.parse("20100816T2231"), datetimePattern);
		// assertFormattedDateEquals("2010-08-16 00:00:00", parser.parse("20100816"), datePattern);
	}

	private void assertFormattedDateEquals(String expected, Object actual, String expectedPattern) {
		final FormattedDate formattedDate = (FormattedDate) actual;
		final Date expectedDate = date(expected);
		final String message = "expected: " + (expectedDate == null ? null : FormattedDate.toStringISO(expectedDate))
		        + "!=" + (formattedDate == null ? null : FormattedDate.toStringISO(formattedDate));
		final Date actualDate = formattedDate == null ? null : new Date(formattedDate.getTime());
		assertEquals(message, expectedDate, actualDate);
		if (actual != null)
			assertEquals("wrong formatter pattern", expectedPattern, formattedDate.getPattern());
	}

	private Date date(String string) {
		try {
			if (string == null)
				return null;
			else if (string.matches(".*[-+]\\d{4}"))
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").parse(string);
			else
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private String getLocalTimeZoneOffsetString() {
		final TimeZone timeZone = TimeZone.getDefault();
		int offsetMinutes = (timeZone.getRawOffset() + timeZone.getDSTSavings()) / 60000;
		return String.format("%+03d%02d", offsetMinutes / 60, offsetMinutes % 60);
	}

	@Test
	public void testDateFormatParser_en() {
		scannerController.selectScanner(new Locale("en"));
		ScannerController parser = scannerController;
		//
		// "M/d"
		//
		// short month
		assertFormattedDateEquals(String.format("%tY-02-21 00:00:00", new Date()), parser.parse("2/21"), datePattern);
		// full month
		assertFormattedDateEquals(String.format("%tY-02-21 00:00:00", new Date()), parser.parse("02/21"), datePattern);
		//
		// "M/d/y"
		//
		// short month and year
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("2/21/10"), datePattern);
		// full year
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("2/21/2010"), datePattern);
		// full
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("02/21/2010"), datePattern);
		// SimpleDateFormat ignores leading whitespace but DateFormatParser does not
		assertParseFailure("whitespace should not be ignored", parser, " 2/21/2011");
		assertParseFailure("whitespace should not be ignored", parser, "2 /21/2011");
		assertParseFailure("whitespace should not be ignored", parser, "2/21/2011 ");
	}

	@Test
	public void testDateFormatParser_de() {
		scannerController.selectScanner(new Locale("de"));
		ScannerController parser = scannerController;
		//
		// "d.M"
		//
		// short month
		assertFormattedDateEquals(String.format("%tY-02-21 00:00:00", new Date()), parser.parse("21.2"), datePattern);
		// full month
		assertFormattedDateEquals(String.format("%tY-02-21 00:00:00", new Date()), parser.parse("21.02"), datePattern);
		//
		// "d.M.y"
		//
		// short month and year
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("21.2.10"), datePattern);
		// full year
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("21.2.2010"), datePattern);
		// full
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("21.02.2010"), datePattern);
		// SimpleDateFormat ignores leading whitespace but DateFormatParser does not
		// SimpleDateFormat ignores leading whitespace but DateFormatParser does not
		assertParseFailure("whitespace should not be ignored", parser, " 21.2.2011");
		assertParseFailure("whitespace should not be ignored", parser, "21 .2.2011");
		assertParseFailure("whitespace should not be ignored", parser, "21.2.2011 ");
	}
}
