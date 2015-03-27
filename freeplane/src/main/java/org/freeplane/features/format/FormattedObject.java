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

import org.freeplane.core.util.FactoryMethod;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.SerializationMethod;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.TypeReference;

/**
 * A generic multi purpose IFormattedObject but mainly for the formatting of strings.
 * @author vboerchers
 */
@FactoryMethod("deserialize")
@SerializationMethod("serialize")
public class FormattedObject implements IFormattedObject {
	private final Object object;
	private final String formattedString;
	private final PatternFormat patternFormat;

	/** tries to guess the right pattern formatter from format.
	 * @throws IllegalArgumentException if value is not formattable with format. */
	public FormattedObject(final Object value, final String format) {
		this(value, PatternFormat.guessPatternFormat(format));
	}

	/** @throws IllegalArgumentException if value is not formattable with format. */
	public FormattedObject(final Object object, final PatternFormat format) {
		this.patternFormat = format;
		this.object = object;
		validate();
		this.formattedString = String.valueOf(patternFormat.formatObject(object));
	}

	private void validate() {
		if (patternFormat == null) {
			throw new IllegalArgumentException(TextUtils.getText("format_invalid_pattern"));
		}
	}

	public String getPattern() {
		return patternFormat.getPattern();
	}

	public Object getObject() {
		return object;
	}

	public static String serialize(final FormattedObject formattedObject) {
		return serializeUnformattedObject(formattedObject) + "|" + formattedObject.patternFormat.serialize();
	}

	private static String serializeUnformattedObject(final FormattedObject formattedObject) {
		//	String unformattedObject;
		//	if (formattedObject.object instanceof IFormattedObject)
		//		unformattedObject = TypeReference.toSpec(formattedObject.object);
		//	else
		//		unformattedObject = formattedObject.object == null ? "" : formattedObject.object.toString();
		return TypeReference.encode(TypeReference.toSpec(formattedObject.object));
	}

	public static Object deserialize(final String text) {
		try {
			final int index = text.indexOf('|');
			final Object object = deserializeUnformattedObject(text, index);
			return new FormattedObject(object, PatternFormat.deserialize(text.substring(index + 1)));
		}
		catch (Exception e) {
			LogUtils.warn("cannot deserialize " + text, e);
			return text;
		}
	}

	private static Object deserializeUnformattedObject(final String text, final int index) {
	    final String spec = text.substring(0, index);
		return TypeReference.create(TypeReference.decode(spec), false);
    }

	@Override
	public String toString() {
		return formattedString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((patternFormat == null) ? 0 : patternFormat.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormattedObject other = (FormattedObject) obj;
		if (patternFormat == null) {
			if (other.patternFormat != null)
				return false;
		}
		else if (!patternFormat.equals(other.patternFormat))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		}
		else if (!object.equals(other.object))
			return false;
		return true;
	}
}
