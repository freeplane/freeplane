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

import java.text.DecimalFormat;

import org.freeplane.core.util.FactoryMethod;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.SerializationMethod;

/**
 * @author vboerchers
 */
@SuppressWarnings("serial")
@FactoryMethod("deserialize")
@SerializationMethod("serialize")
public class FormattedNumber extends Number implements IFormattedObject, Comparable<Number> {
	private final Number number;
	private final String pattern;
	private final String formattedString;
	private final boolean isDefaultFormat;

	public FormattedNumber(final Number number) {
		this(number, FormatController.getController().getDefaultNumberFormat());
	}

	public FormattedNumber(final Number number, final String pattern) {
		this(number, pattern, number == null ? null //
		        : FormatController.getController().getDecimalFormat(pattern).format(number));
	}

	public FormattedNumber(final Number number, final String pattern, final String formattedString) {
		this(number, pattern, formattedString, false);
	}

	// implementation detail
	private FormattedNumber(final Number number, final DecimalFormat format) {
		this(number, format, true);
	}

	// implementation detail
	private FormattedNumber(final Number number, final DecimalFormat format, final boolean isDefault) {
		this(number, format.toPattern(), number == null ? null : format.format(number), isDefault);
	}

	// implementation detail
	private FormattedNumber(final Number number, final String pattern, final String formattedString,
	                        final boolean isDefault) {
		if(number instanceof Float || number instanceof  Double) {
			final long longValue = number.longValue();
			if (longValue == number.doubleValue()) {
				if(longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE)
					this.number = (int)longValue;
				else
					this.number = longValue;
			}
			else
				this.number = number;
		}
		else
			this.number = number;
		this.pattern = pattern;
		this.formattedString = formattedString;
		this.isDefaultFormat = isDefault;
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

	/** default formats are not saved to file. */
	public static String serialize(final FormattedNumber formattedNumber) {
		return formattedNumber.number + (formattedNumber.isDefaultFormat ? "" : "|" + formattedNumber.pattern);
	}

	public static Object deserialize(final String text) {
		try {
			final int index = text.indexOf('|');
			if (index == -1)
				return new FormattedNumber(Double.parseDouble(text));
			else
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

	public int compareTo(final Number that) {
		if (this.number != null && that != null)
			return Double.compare(this.number.doubleValue(), that.doubleValue());
		return (number == null ? 0 : 1) - (that == null ? 0 : 1);
	}
}
