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

import java.util.Date;
import java.util.Locale;

import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedNumber;
import org.freeplane.features.format.FormattedObject;
import org.junit.Before;
import org.junit.Test;

/**
 * @author vboerchers
 */
public class FormattedObjectTest {
	@Before
	public void setup() {
		Locale.setDefault(new Locale("en"));
	}

	@Test
	public void testBasics() {
		assertEquals("wrong formatting", "x: bla", new FormattedObject("bla", "x: %s").toString());
		assertEquals("wrong formatting", "x: 1.1234", new FormattedObject(1.1234, "x: %s").toString());
		assertEquals("wrong formatting", "1.12", new FormattedObject(1.1234, "%.2f").toString());
		assertEquals("wrong formatting", "1.12", new FormattedObject(1.1234, "#.##").toString());
	}
	
	@Test
	public void testNestedIFormattedObject() {
		double number = 1.123456;
		final FormattedNumber formattedNumber = new FormattedNumber(number, "#.##");
		final FormattedObject formattedObject  = new FormattedObject(formattedNumber, "x: %s");
		assertEquals("wrong pattern", "x: %s", formattedObject.getPattern());
		assertEquals("wrong formatting", "x: 1.12", formattedObject.toString());
	}

	@Test
	public void testLocaleDependence() {
		final FormattedObject formattedObject = new FormattedObject(1.1234, "%.3f");
		assertEquals("wrong formatting", "1.123", formattedObject.toString());
		Locale.setDefault(new Locale("de"));
		// the format is evaluated only once
		assertEquals("no support for on-the-fly-changes of locale", "1.123", formattedObject.toString());
		// FormattedObject does not caches the formats
		assertEquals("formatting should be locale dependent", "1,123", new FormattedObject(1.1234, "%.3f").toString());
	}

	@Test
	public void testSerializeNumber() {
		double number = 1.123456;
		final FormattedObject formattedObject = new FormattedObject(number, "#.##");
		final String serialized = "java.lang.Double&#x7c;1.123456|number:decimal:#.##";
		assertEquals(serialized, FormattedObject.serialize(formattedObject));
		assertEquals("failure in deserialization", formattedObject, FormattedObject.deserialize(serialized));
	}
	
	@Test
	public void testSerializeString() {
		String unformattedString = "\"sdf\"|sdf&#x7c;sdf&amp;#x7c;: %s";
		final FormattedObject formattedObject = new FormattedObject(unformattedString, "|\"x\"|: %s");
		final String serialized = "&#x22;sdf&#x22;&#x7c;sdf&#x26;#x7c;sdf&#x26;amp;#x7c;: %s|string:formatter:&#x7c;&#x22;x&#x22;&#x7c;: %s";
		assertEquals(serialized, FormattedObject.serialize(formattedObject));
		assertEquals("failure in deserialization", formattedObject, FormattedObject.deserialize(serialized));
	}
	
	@Test
	public void testStrangeInput() {
		final FormattedDate date = new FormattedDate(new Date(), "yyyy-mm-dd");
		final FormattedObject formattedObject = new FormattedObject(date, "#.##");
		assertEquals("decimal format is not applicable to date", date.toString(), formattedObject.toString());
	}
}
