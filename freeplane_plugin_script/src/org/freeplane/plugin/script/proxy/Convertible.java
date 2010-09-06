package org.freeplane.plugin.script.proxy;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.core.util.HtmlUtils;

/** Utility class that is used to convert node texts to different types.
 * <p>
 * <em>Warning:</em> The nodeModel is used for script invocation ({@link #getValue()}), not
 *          for access its properties. Therefore text and nodeModel are not synchronized */
// Unfortunately it seems impossible to implement Comparable<Object> since in this case
// TypeTransformation.compareToWithEqualityCheck() is called and will return false for
//   assert new Comparable(2) == "2"
// instead of just calling equals, which is correctly defined
public class Convertible extends GroovyObjectSupport /*implements Comparable<Object>*/ {
	private static final Pattern DATE_REGEXP_PATTERN = Pattern.compile("\\d{4}(-?)\\d{2}(-?)\\d{2}" //
	        + "(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?");
	private final String text;

	/** doesn't evaluate formulas since this would require a calculation rule or NodeModel. */
	public Convertible(String text) {
		this.text = text;
	}

	/** same as toString(text), i.e. conversion is done properly. */
	public Convertible(Object text) {
		this.text = toString(text);
    }

	/**
	 * returns a Long or a Double, whatever fits best. All Java number literals are allowed as described
	 * by {@link Long#decode(String)}
	 * 
	 * @throws ConversionException if text is not a number.
	 */
	public Number getNum() throws ConversionException {
		try {
			try {
				return text == null ? null : Long.decode(text);
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
		return text == null ? null : HtmlUtils.htmlToPlain(text);
	}

	/**
	 * returns a Date for the parsed text.
	 * The valid date patterns are "yyyy-MM-dd HH:dd:ss.SSSZ" with optional '-', ':'. ' ' may be replaced by 'T'. 
	 * @throws ConversionException if the text is not convertible to a date.
	 */
	public Date getDate() throws ConversionException {
		return text == null ? null : parseDate(text);
	}

	private static Date parseDate(String text) throws ConversionException {
		//        1         2         34            5         6   7        8           9
		// \\d{4}(-?)\\d{2}(-?)\\d{2}(([ T])?\\d{2}(:?)\\d{2}(:?)(\\d{2})?(\\.\\d{3})?([-+]\\d{4})?)?
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
		if (text == null)
			return null;
		final Date date = parseDate(text);
		final GregorianCalendar result = new GregorianCalendar(0, 0, 0);
		result.setTime(date);
		return result;
	}

	/**
	 * Uses the following priority ranking to determine the type of the text:
	 * <ol>
	 * <li>null
	 * <li>Long
	 * <li>Double
	 * <li>Date
	 * <li>String
	 * </ol>
	 * @return Object - the type that fits best.
	 */
	public Object getObject() {
		if (text == null)
			return null;
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

	/** returns true if the text is convertible to number. */
	public boolean isNum() {
		// handles null -> false
		return NumberUtils.isNumber(text);
	}

	/** returns true if the text is convertible to date. */
	public boolean isDate() {
		if (text == null)
			return false;
		final Matcher matcher = DATE_REGEXP_PATTERN.matcher(text);
		return matcher.matches();
	}

	/** pretend we are a String if we don't provide a property for ourselves. */
	public Object getProperty(String property) {
		// called methods should handle null values
		try {
			// disambiguate isNum()/getNum() in favor of getNum()
			if (property.equals("num"))
				return getNum();
			// same for isDate()/getDate()
			if (property.equals("date"))
				return getDate();
			return super.getProperty(property);
		}
		catch (ConversionException e) {
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			return InvokerHelper.getMetaClass(String.class).getProperty(text, property);
		}
	}

	/** pretend we are a String if we don't provide a method for ourselves. */
	public Object invokeMethod(String name, Object args) {
		try {
			// called methods should handle null values
			return super.invokeMethod(name, args);
		}
		catch (MissingMethodException mme) {
			return InvokerHelper.getMetaClass(String.class).invokeMethod(text, name, args);
		}
	}

	/** has special conversions for
	 * <ul>
	 * <li>Date and Calendar are converted by
	 *     org.apache.commons.lang.time.DateFormatUtils.formatUTC(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ"), i.e. to
	 *     GMT timestamps, e.g.: "2010-08-16 22:31:55.123+0000".
	 * <li>null is "converted" to null
	 * </ul>
	 * All other types are converted via value.toString().
	 */
	public static String toString(Object value) {
		if (value == null)
			return null;
		else if (value instanceof Date)
			return Convertible.dateToString(((Date) value));
		else if (value instanceof Calendar)
			return Convertible.dateToString(((Calendar) value).getTime());
		else
			return value.toString();
	}

	private static String dateToString(Date date) {
		return DateFormatUtils.formatUTC(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}
	
	// Unfortunately it seems impossible to implement Comparable<Object> since in this case
	// TypeTransformation.compareToWithEqualityCheck() is called and will return false for
	//   assert new Comparable(2) == "2"
	// instead of just calling equals, which is correctly defined
	public int compareTo(Object string) {
		if (string.getClass() == String.class)
			return text.compareTo((String) string);
		else
			return 1;
	}
	
	public int compareTo(Convertible convertible) {
		return text.compareTo(convertible.getText());
	}

	/** since equals handles Strings special we have to stick to that here too since
	 * equal objects have to have the same hasCode. */
	@Override
	public int hashCode() {
		return text == null ? 0 : text.hashCode();
	}

	/** note: if obj is a String the result is true if String.equals(text). */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() == String.class)
			return text.equals(obj);
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
		return text;
	}

	@Override
    public void setProperty(String property, Object newValue) {
		throw new NotImplementedException("Convertibles are immutable");
    }
}
