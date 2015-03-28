/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.attribute;

/**
 * @author Dimitry Polivaev
 */
public class Attribute {
	private String name;
	private Object value;

	/**
	 * @param pAttribute
	 *            deep copy.
	 */
	public Attribute(final Attribute pAttribute) {
		name = pAttribute.name;
		value = pAttribute.value;
	}

	/**
	 */
	public Attribute(final String name) {
		this.name = name;
		value = "";
	}

	public Attribute(final String name, final Object value) {
		this.name = name;
		setValue(value);
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setValue(final Object value) {
		if(value == null)
			throw new NullPointerException();
		this.value = value;
	}

	@Override
	public String toString() {
		return "[" + name + ", " + value + "]";
	}
}
