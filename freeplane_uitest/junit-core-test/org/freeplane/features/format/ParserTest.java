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

import org.freeplane.features.format.DateFormatParser;
import org.freeplane.features.format.DecimalFormatParser;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedNumber;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.format.IsoDateParser;
import org.freeplane.features.format.NumberLiteralParser;
import org.freeplane.main.application.FreeplaneStarter;
import org.freeplane.main.mindmapmode.MModeControllerFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Volker Boerchers
 */
public class ParserTest {
	private static final String TYPE_DATE = IFormattedObject.TYPE_DATE;
	private static final String TYPE_DATETIME = IFormattedObject.TYPE_DATETIME;
	private static String datePattern;
	private static String datetimePattern;
	private static FormatController formatController;

	@BeforeClass
	public static void initStatics() {
		// FIXME: we have to start Freeplane to create a Controller for script execution
		System.setProperty("org.freeplane.nosplash", "true");
		new FreeplaneStarter().createController();
		MModeControllerFactory.createModeController();
		formatController = FormatController.getController();
		datePattern = ((SimpleDateFormat) formatController.getDefaultFormat(TYPE_DATE)).toPattern();
		datetimePattern = ((SimpleDateFormat) formatController.getDefaultFormat(TYPE_DATETIME)).toPattern();
	}

	@Test
	public void testNumberLiteralParser() throws Exception {
		NumberLiteralParser parser = new NumberLiteralParser();
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
		// invalid symbols
		assertFormattedNumberEquals("parser should not recognize thousand separator", null, parser.parse("99,999"));
		assertFormattedNumberEquals("parser should not recognize thousand separator", null, parser.parse("999,999.99"));
		// invalid chars
		assertFormattedNumberEquals("parser should not skip invalid chars", null, parser.parse("999x.12345"));
		assertFormattedNumberEquals("parser should not skip invalid chars", null, parser.parse("x0.12345"));
		assertFormattedNumberEquals("parser should not skip invalid chars", null, parser.parse("-1.12345x"));
		// whitespace
		assertFormattedNumberEquals("parser should not ignore whitespace", null, parser.parse("999 .12345"));
		assertFormattedNumberEquals("parser should not ignore whitespace", null, parser.parse("-1.12345 "));
		assertFormattedNumberEquals("parser should not ignore whitespace", null, parser.parse(" 0.12345"));
	}

	private void testDecimalParserImpl(final Locale locale, final char sep, final char thousand_sep) {
		DecimalFormatParser parser = new DecimalFormatParser(locale);
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
		assertFormattedNumberEquals("parser should not skip invalid chars", null, parser.parse("999x" + sep + "12345"));
		assertFormattedNumberEquals("parser should not skip invalid chars", null, parser.parse("x0" + sep + "12345"));
		assertFormattedNumberEquals("parser should not skip invalid chars", null, parser.parse("-1" + sep + "12345x"));
		// whitespace
		assertFormattedNumberEquals("parser should not ignore whitespace", null, parser.parse("999 " + sep + "12345"));
		assertFormattedNumberEquals("parser should not ignore whitespace", null, parser.parse("-1" + sep + "12345 "));
		assertFormattedNumberEquals("parser should not ignore whitespace", null, parser.parse(" 0" + sep + "12345"));
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
		if (expected != actual)
			assertEquals(expected, toDouble(actual), 1e-7);
	}

	private void assertFormattedNumberEquals(String message, Double expected, Object actual) {
		if (expected != actual)
			assertEquals(message, expected, toDouble(actual), 1e-7);
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
		IsoDateParser parser = new IsoDateParser();
		// null safe
		assertFormattedDateEquals(null, parser.parse(null), datetimePattern);
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
		//
		assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("20100816223155.000"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("20100816223155"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:00", parser.parse("201008162231"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 00:00:00", parser.parse("20100816"), datePattern);
		//
		assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("20100816T223155.000"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:55", parser.parse("20100816T223155"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 22:31:00", parser.parse("20100816T2231"), datetimePattern);
		assertFormattedDateEquals("2010-08-16 00:00:00", parser.parse("20100816"), datePattern);
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
	public void testDateFormatParser() throws Exception {
		//
		// "M/d"
		//
		DateFormatParser parser = new DateFormatParser("M/d");
		// null safe
		assertFormattedDateEquals(null, parser.parse(null), datePattern);
		// short month
		assertFormattedDateEquals(String.format("%tY-02-21 00:00:00", new Date()), parser.parse("2/21"), datePattern);
		// full month
		assertFormattedDateEquals(String.format("%tY-02-21 00:00:00", new Date()), parser.parse("02/21"), datePattern);
		// parser should parse the whole input
		assertFormattedDateEquals(null, parser.parse("2/21/2011"), datePattern);
		//
		// "M/d/y"
		//
		parser = new DateFormatParser("M/d/y");
		// null safe
		assertFormattedDateEquals(null, parser.parse(null), datePattern);
		// short month and year
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("2/21/10"), datePattern);
		// full year
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("2/21/2010"), datePattern);
		// full
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("02/21/2010"), datePattern);
		// SimpleDateFormat ignores leading whitespace but DateFormatParser does not
		assertFormattedDateEquals(null, parser.parse(" 2/21/2011"), datePattern);
		assertFormattedDateEquals(null, parser.parse("2 /21/2011"), datePattern);
		assertFormattedDateEquals(null, parser.parse("2/21/2011 "), datePattern);
		// illegal dates
		assertFormattedDateEquals(null, parser.parse("0/21/2010"), datePattern);
		assertFormattedDateEquals(null, parser.parse("13/21/2010"), datePattern);
		assertFormattedDateEquals(null, parser.parse("12/32/2010"), datePattern);
		//
		// leading spaces: " M/d/y"
		//
		parser = new DateFormatParser(" M/d/y");
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse(" 02/21/2010"), datePattern);
		assertFormattedDateEquals("2010-02-21 00:00:00", parser.parse("02/21/2010"), datePattern);
		assertFormattedDateEquals(null, parser.parse("02/21/2010 "), datePattern);
	}
}
