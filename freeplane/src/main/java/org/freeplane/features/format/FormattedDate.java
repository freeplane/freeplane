/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.util.FactoryMethod;
import org.freeplane.core.util.SerializationMethod;

/**
 * @author Dimitry Polivaev
 * Mar 2, 2011
 */
@SuppressWarnings("serial")
@FactoryMethod("deserialize")
@SerializationMethod("serialize")
public class FormattedDate extends Date implements IFormattedObject {
	public static final String ISO_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
	public static final String ISO_DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mmZ";
	public static final Pattern ISO_DATE_TIME_REGEXP_PATTERN = Pattern.compile("\\d{4}(-?)\\d{2}(-?)\\d{2}" //
	        + "(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?");
	private SimpleDateFormat df;
	private String defaultType;

	public FormattedDate(FormattedDate date) {
		this(date.getTime(), date.getDateFormat());
	}

	public FormattedDate(Date date, String pattern) {
		super(date.getTime());
		this.df = FormatController.getController().getDateFormat(pattern);
	}

	public FormattedDate(long date, SimpleDateFormat df) {
		super(date);
		this.df = df;
	}

	/**@deprecated use {@link #createDefaultFormattedDate(long, String)} instead. */
	public FormattedDate(long date) {
		this(date, FormatController.getController().getDefaultDateFormat());
		this.defaultType = IFormattedObject.TYPE_DATE;
	}

	public static FormattedDate createDefaultFormattedDate(long time, String type) {
		final FormattedDate formattedDate = new FormattedDate(time,
		    (SimpleDateFormat) FormatController.getController().getDefaultFormat(type));
		formattedDate.defaultType = type;
		return formattedDate;
	}

	@Override
	public String toString() {
		return df.format(this);
	}

	/** default formats are not saved to file. */
	public static String serialize(final FormattedDate date) {
		return toStringISO(date) + "|" + (date.defaultType != null ? date.defaultType : date.df.toPattern());
	}

	public static String toStringISO(final Date date) {
		// use local timezone
		return FormatController.getController().getDateFormat(ISO_DATE_TIME_FORMAT_PATTERN).format(date);
	}

	public static String toStringShortISO(final Date date) {
		return FormatController.getController().getDateFormat(ISO_DATE_FORMAT_PATTERN).format(date);
	}

	public static Object deserialize(String text) {
		final int index = text.indexOf('|');
		final FormattedDate date;
		final String arg;
		if (index == -1) {
			date = toDateISO(text);
			arg = IFormattedObject.TYPE_DATE;
		}
		else {
			date = toDateISO(text.substring(0, index));
			arg = text.substring(index + 1);
		}
		if (date == null)
			return text;
		if (arg.equals(IFormattedObject.TYPE_DATE) || arg.equals(IFormattedObject.TYPE_DATETIME)) {
			date.defaultType = arg;
			date.df = (SimpleDateFormat) FormatController.getController().getDefaultFormat(arg);
		}
		else {
			date.df = FormatController.getController().getDateFormat(arg);
		}
		return date;
	}

	public static FormattedDate toDate(String text) {
		return toDateISO(text);
	}

	public static boolean isDate(String text) {
		if (text == null)
			return false;
		return ISO_DATE_TIME_REGEXP_PATTERN.matcher(text).matches();
	}

	public static FormattedDate toDateISO(String text) {
		//        1         2         34            5         6   7        8           9
		// \\d{4}(-?)\\d{2}(-?)\\d{2}(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?
		final Matcher matcher = ISO_DATE_TIME_REGEXP_PATTERN.matcher(text);
		if (matcher.matches()) {
			StringBuilder builder = new StringBuilder("yyyy");
			builder.append(matcher.group(1));
			builder.append("MM");
			builder.append(matcher.group(2));
			builder.append("dd");
			if (matcher.group(3) != null) {
				if (matcher.group(4) != null) {
					builder.append('\'');
					builder.append(matcher.group(4));
					builder.append('\'');
				}
				builder.append("HH");
				builder.append(matcher.group(5));
				builder.append("mm");
				if (matcher.group(7) != null) {
					builder.append(matcher.group(6));
					builder.append("ss");
				}
				if (matcher.group(8) != null) {
					builder.append(".SSS");
				}
				if (matcher.group(9) != null) {
					builder.append("Z");
				}
			}
			final String pattern = builder.toString();
			return parseDate(text, pattern);
		}
		return null;
	}

	static private FormattedDate parseDate(String text, final String pattern) {
		SimpleDateFormat parser = FormatController.getController().getDateFormat(pattern);
		final ParsePosition pos = new ParsePosition(0);
		final Date date = parser.parse(text, pos);
		if (date != null && pos.getIndex() == text.length()) {
			return new FormattedDate(date.getTime(), parser);
		}
		return null;
	}

	public boolean containsTime() {
		return df.toPattern().contains("m");
	}

	public SimpleDateFormat getDateFormat() {
		return df;
	}

	public String getPattern() {
		return df.toPattern();
	}

	public Date getObject() {
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof FormattedDate && super.equals(obj) && ((FormattedDate) obj).getDateFormat().equals(df);
	}

	@Override
	public int hashCode() {
		return 37 * super.hashCode() + df.hashCode();
	}
}
