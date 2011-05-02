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
package org.freeplane.features.common.format;

import static org.junit.Assert.*;

import java.util.Locale;

import org.freeplane.features.common.format.FormattedNumber;
import org.junit.Before;
import org.junit.Test;

/**
 * @author vboerchers
 */
public class FormattedNumberTest {
	@Before
	public void setup() {
		Locale.setDefault(new Locale("en"));
	}

	@Test
	public void testDefaultPattern() {
		double number = 1.123459;
		final FormattedNumber formattedNumber = new FormattedNumber(number);
		assertEquals("wrong default pattern", "#.#####", formattedNumber.getPattern());
		// expect a rounded number
		assertEquals("wrong default formatting", "1.12346", formattedNumber.toString());
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
}
