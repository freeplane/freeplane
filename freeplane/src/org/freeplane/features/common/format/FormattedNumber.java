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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import org.freeplane.core.util.FactoryMethod;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.SerializationMethod;

/**
 * @author vboerchers
 */
@SuppressWarnings("serial")
@FactoryMethod("deserialize")
@SerializationMethod("serialize")
public class FormattedNumber extends Number implements IFormattedObject {
	private static HashMap<String, NumberFormat> formatCache = new HashMap<String, NumberFormat>();
	private final Number number;
	private final String pattern;
	private String formattedString;
	public static final String defaultPattern = "#.#####";

	public FormattedNumber(final Number number) {
		this(number, defaultPattern);
	}
	
	public FormattedNumber(final Number number, final String pattern) {
		this(number, pattern, number == null ? null : getDecimalFormat(pattern).format(number));
	}

	public FormattedNumber(final Number number, final String pattern, final String formattedString) {
		this.number = number;
		this.pattern = pattern;
		this.formattedString = formattedString;
    }

	public Number getNumber() {
    	return number;
    }

	public String getPattern() {
    	return pattern;
    }

	/** implementation of {@link IFormattedObject}. */
	public Object getObject() {
	    return number;
    }

	private static NumberFormat getDecimalFormat(final String pattern) {
		NumberFormat format = formatCache.get(pattern);
		if (format == null) {
			format = (pattern == null) ? DecimalFormat.getInstance() : new DecimalFormat(pattern);
			formatCache.put(pattern, format);
		}
		return format;
	}

	public static String serialize(final FormattedNumber formattedNumber) {
    	return formattedNumber.number + "|" + formattedNumber.pattern;
    }

	public static Object deserialize(final String text) {
		try {
			final int index = text.indexOf('|');
			return new FormattedNumber(Double.parseDouble(text.substring(0, index)), text.substring(index + 1));
		}
		catch (Exception e) {
			LogUtils.warn("cannot deserialize " + text, e);
			return text;
		}
	}

	@Override
    public String toString() {
	    return formattedString;
    }

	@Override
	public int intValue() {
		return number.intValue();
	}

	@Override
	public long longValue() {
		return number.longValue();
	}

	@Override
	public float floatValue() {
		return number.floatValue();
	}

	@Override
	public double doubleValue() {
		return number.doubleValue();
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((number == null) ? 0 : number.hashCode());
	    result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
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
	    FormattedNumber other = (FormattedNumber) obj;
	    if (number == null) {
		    if (other.number != null)
			    return false;
	    }
	    else if (!number.equals(other.number))
		    return false;
	    if (pattern == null) {
		    if (other.pattern != null)
			    return false;
	    }
	    else if (!pattern.equals(other.pattern))
		    return false;
	    return true;
    }
}
