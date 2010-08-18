package org.freeplane.plugin.script.proxy;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.util.HtmlUtils;

/** Utility class that is used to convert node texts to different types.
 * <p>
 * <em>Warning:</em> The nodeModel is used for script invocation ({@link #getValue()}), not
 *          for access its properties. Therefore text and nodeModel are not synchronized */
public class Convertible {
	private static final Pattern DATE_REGEXP_PATTERN = Pattern.compile("\\d{4}(-?)\\d{2}(-?)\\d{2}"
		+ "(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?)?");
	private final String text;
	
	public Convertible(String text) {
		this.text = text;
	}

	/**
	 * returns a Long or a Double, whatever fits best.
	 * @throws ConversionException if text is not a number.
	 */
	public Number getNum() throws ConversionException {
		try {
			try {
				return Long.valueOf(text);
			}
			catch (NumberFormatException e) {
				return Double.valueOf(text);
			}
		}
		catch (NumberFormatException e) {
			throw new ConversionException("not a number: '" + text + "'", e);
		}
	}

	public String getString() {
		return text;
	}

	public String getText() {
		return text;
	}

	public String getPlain() {
		return HtmlUtils.htmlToPlain(text);
	}

	/**
	 * returns a Date for the parsed text.
	 * The valid date patterns are "yyyy-MM-dd hh:dd:ss" with optional '-', ':' and 'T' instead of ' '. 
	 * @throws ConversionException if the text is not convertible to a date.
	 */
	public Date getDate() throws ConversionException {
		return parseDate(text);
	}

	private static Date parseDate(String text) throws ConversionException {
		//        1         2         34            5         6   7
		// \\d{4}(-?)\\d{2}(-?)\\d{2}(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?)?
		final Matcher matcher = DATE_REGEXP_PATTERN.matcher(text);
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
				builder.append("hh");
				builder.append(matcher.group(5));
				builder.append("mm");
				if (matcher.group(7) != null) {
					builder.append(matcher.group(6));
					builder.append("ss");
				}
			}
			SimpleDateFormat parser = new SimpleDateFormat(builder.toString());
			ParsePosition pos = new ParsePosition(0);
			Date date = parser.parse(text, pos);
			if (date != null && pos.getIndex() == text.length()) {
				return date;
			}
		}
		throw new ConversionException("not a date: " + text);
	}

	/**
	 * returns a Calendar for the parsed text. 
	 * @throws ConversionException if the text is not convertible to a date.
	 */
	public Calendar getCalendar() throws ConversionException {
		final Date date = parseDate(text);
		if (date == null)
			return null;
		final GregorianCalendar result = new GregorianCalendar(0, 0, 0);
		result.setTime(date);
		return result;
	}

	/**
	 * Uses the following priority ranking to determine the type of the text:
	 * <ol>
	 * <li>Formula: evaluation result
	 * <li>Long
	 * <li>Double
	 * <li>Date
	 * <li>String
	 * </ol>
	 * @return Object - the type that fits best.
	 */
	public Object getObject() {
		if (text.length() > 1 && text.charAt(0) == '=')
			return getValue();
		try {
			return getNum();
		}
		catch (ConversionException e1) {
			try {
				return getDate();
			}
			catch (ConversionException e2) {
				return text;
			}
		}
	}

	/** without a calculation rule or NodeModel - there's nothing to evaluate. */
	public Object getValue() {
		return text;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((text == null) ? 0 : text.hashCode());
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
	    Convertible other = (Convertible) obj;
	    if (text == null) {
		    if (other.text != null)
			    return false;
	    }
	    else if (!text.equals(other.text))
		    return false;
	    return true;
    }

	@Override
    public String toString() {
	    return '"' + text + "\".to";
    }
}
