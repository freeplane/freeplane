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

import java.util.Locale;

import org.freeplane.features.format.FormattedNumber;
import org.freeplane.main.application.FreeplaneStarter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author vboerchers
 */
public class FormattedNumberTest {
	@BeforeClass
	public static void initStatics() {
		// FIXME: we have to start Freeplane to create a Controller for script execution
		System.setProperty("org.freeplane.nosplash", "true");
		new FreeplaneStarter().createController();
	}
	
	@Before
	public void setup() {
		Locale.setDefault(new Locale("en"));
	}

	@Test
	public void testDefaultPattern() {
		double number = 1.123456;
		final FormattedNumber formattedNumber = new FormattedNumber(number);
		assertEquals("wrong default pattern", "#0.####", formattedNumber.getPattern());
		// expect a rounded number
		assertEquals("wrong default formatting", "1.1235", formattedNumber.toString());
	}

	@Test
	public void testWithPattern() {
		double number = 1.123456;
		final FormattedNumber formattedNumber = new FormattedNumber(number, "#.##");
		assertEquals("wrong pattern", "#.##", formattedNumber.getPattern());
		assertEquals("wrong formatting", "1.12", formattedNumber.toString());
	}
	
	@Test
	public void testLocaleDependence() {
		double number = 1.123456;
		final FormattedNumber formattedNumber = new FormattedNumber(number, "#.##");
		assertEquals("wrong formatting", "1.12", formattedNumber.toString());
		Locale.setDefault(new Locale("de"));
		// FormattedNumber evaluates the format only once
		assertEquals("no support for on-the-fly-changes of locale", "1.12", formattedNumber.toString());
		// FormattedNumber caches the formats and formats evaluate the locale only once
		assertEquals("no support for on-the-fly-changes of locale", "1.12", new FormattedNumber(number, "#.##").toString());
		// a new pattern - not cached
		assertEquals("formatting should be locale dependent", "1,12", new FormattedNumber(number, "#.00").toString());
	}

	@Test
	public void testSerialization() {
		double number = 1.123456;
		final FormattedNumber formattedNumber = new FormattedNumber(number, "#.##");
		final String serialized = "1.123456|#.##";
		assertEquals(serialized, FormattedNumber.serialize(formattedNumber));
		assertEquals(formattedNumber, FormattedNumber.deserialize(serialized));
	}
	
	@Test
	public void testCompareTo() {
		final FormattedNumber formattedNumberNull = new FormattedNumber(null);
		Double number1 = 1.123456;
		final FormattedNumber formattedNumber1 = new FormattedNumber(number1);
		Double number2 = 1.123457;
		final FormattedNumber formattedNumber2 = new FormattedNumber(number2);
		assertEquals(-1, number1.compareTo(number2));
		assertEquals(-1, formattedNumber1.compareTo(formattedNumber2));
		assertEquals(-1, formattedNumberNull.compareTo(formattedNumber2));
		assertEquals(1, formattedNumber1.compareTo(null));

		assertEquals(1, number2.compareTo(number1));
		assertEquals(1, formattedNumber2.compareTo(formattedNumber1));
		// NPE (OK - only limited support for nulls): assertEquals(1, formattedNumber2.compareTo(formattedNumberNull));
		assertEquals(-1, formattedNumberNull.compareTo(formattedNumber1));
	}
}
