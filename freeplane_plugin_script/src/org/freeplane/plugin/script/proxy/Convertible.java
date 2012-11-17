package org.freeplane.plugin.script.proxy;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.format.FormattedDate;

/** Utility class that is used to convert node texts to different types.
 * It's especially important for Formulas. */
// Unfortunately it seems impossible to implement Comparable<Object> since in this case
// TypeTransformation.compareToWithEqualityCheck() is called and will return false for
//   assert new Comparable(2) == "2"
// instead of just calling equals, which is correctly defined
public class Convertible extends GroovyObjectSupport /*implements Comparable<Object>*/ {
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
			return TextUtils.toNumber(text);
		}
		catch (NumberFormatException e) {
			throw new ConversionException("not a number: '" + text + "'", e);
		}
	}


	/**
	 * "Safe" variant of getNum(): returns a Long or a Double if text is convertible to it or 0 otherwise
	 * (even if text is null).
	 * 
	 * @throws nothing - on any error (long) 0 is returned.
	 */
	public Number getNum0() {
	    try {
	        final Number result = getNum();
			return result == null ? 0L : result;
        }
        catch (Exception e) {
        	return 0L;
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
		final Date date = FormattedDate.toDateISO(text);
		if(date != null)
			return date;
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

    public URI getUri() throws ConversionException {
        if (text == null)
            return null;
        try {
            if (TextUtils.matchUriPattern(text))
                return new URI(text);
        }
        catch (URISyntaxException e) {
            // throw below
        }
        throw new ConversionException("not an uri: " + text);
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
	            try {
	                return getUri();
	            }
	            catch (ConversionException e3) {
	                return text;
	            }
			}
		}
	}

    /** Allow statements like this: <code>node['attr_name'].to.num</code>. */
	public Convertible getTo() {
		return this;
	}

	/** returns true if the text is convertible to number. */
	public boolean isNum() {
		// handles null -> false
		return TextUtils.isNumber(text);
	}

	/** returns true if the text is convertible to date. */
	public boolean isDate() {
		return FormattedDate.isDate(text);
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
			if (property.equals("uri"))
			    return getUri();
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
	 *     org.apache.commons.lang.time.DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ"), i.e. to
	 *     GMT timestamps, e.g.: "2010-08-16T22:31:55.123+0000".
	 * <li>null is "converted" to null
	 * </ul>
	 * All other types are converted via value.toString().
	 */
	public static String toString(Object value) {
		if (value == null)
			return null;
		else if (value.getClass().equals(String.class))
			return (String) value;
		else if (value instanceof Date)
			return FormattedDate.toStringISO(((Date) value));
		else if (value instanceof Calendar)
			return FormattedDate.toStringISO(((Calendar) value).getTime());
		else
			return value.toString();
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
			return text == null;
		if (obj.getClass() == String.class && text != null)
			return text.equals(obj);
		if (!(obj instanceof Convertible))
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
