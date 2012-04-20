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

import org.junit.Test;

/**
 * @author vboerchers
 */
public class FormattedFormulaTest {
	@Test
	public void testBasics() {
		assertEquals("wrong formatting", "= 1 + 2.333", new FormattedFormula("= 1 + 2.333", "0.1f").toString());
		// FIXME: add an example of the formatting of a formula result
	}
	
	@Test
	public void testDeSerialize() {
		final String formula = "= 'hurz'";
        final String pattern = "hurz - %s!";
        String serialized = formula + "|" +pattern;
		final Object deserialized = FormattedFormula.deserialize(serialized);
		assertEquals(FormattedFormula.class, deserialized.getClass());
        final FormattedFormula formattedObject = (FormattedFormula) deserialized;
		assertEquals(formula, formattedObject.getObject());
		assertEquals(pattern, formattedObject.getPattern());
	}
	
	@Test
	public void testSerialize() {
        final String formula = "= 'hurz'";
        final String pattern = "hurz - %s!";
	    final FormattedFormula formattedObject = new FormattedFormula(formula, pattern);
        String serialized = formula + "|" +pattern;
	    assertEquals(serialized, FormattedFormula.serialize(formattedObject));
	    assertEquals("failure in deserialization", formattedObject, FormattedFormula.deserialize(serialized));
	}
}
