package org.freeplane.api;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;



/** Utility wrapper class around a String that is used to convert node texts to different types.
 * It's especially important for Formulas. */
// Unfortunately it seems impossible to implement Comparable<Object> since in this case
// TypeTransformation.compareToWithEqualityCheck() is called and will return false for
//   assert new Comparable(2) == "2"
// instead of just calling equals, which is correctly defined
public interface Convertible {

	/** Convert to Number. All Java number literals are allowed as described by {@link Long#decode(String)}
	 * @return a Long or a Double, whatever fits best.
	 * @throws ConversionException if text is not a number. */
	public Number getNum() throws ConversionException;


	/** Safe variant of {@link #getNum()}, throws nothing - on any error (long) 0 is returned.
	 * @return a Long or a Double if text is convertible to it or 0 otherwise (even if text is null). */
	public Number getNum0();

	/** No conversion.
	 * @return The original string. */
	public String getString();

	/** No conversion.
	 * @return The original string. */
	public String getText();

	/** Removes HTML markup if necessary.
	 * @return The result of {@link HtmlUtils#htmlToPlain(String)} */
	public String getPlain();

	/** Converts to Date if possible. The valid date patterns are "yyyy-MM-dd HH:dd:ss.SSSZ"
	 * with optional '-', ':'. ' ' may be replaced by 'T'.
	 * @return a Date for the parsed text
	 * @throws ConversionException if the text is not convertible to a Date. */
	public Date getDate() throws ConversionException;

	/** Converts to Calendar if possible. See {@link #getDate()} for recognized patterns.
	 * @return a Calendar for the parsed text.
	 * @throws ConversionException if the text is not convertible to a Date. */
	public Calendar getCalendar() throws ConversionException;

	/** Converts to URI if possible.
	 * @return a URI
	 * @throws ConversionException if the text is not convertible to a URI. */
    public URI getUri() throws ConversionException;

	/** Uses the following priority ranking to determine the type of the text:
	 * <ol>
	 * <li>null
	 * <li>Long
	 * <li>Double
	 * <li>Date
	 * <li>String
	 * </ol>
	 * @return Object - the type that fits best. */
	public Object getObject();


	/** Type check.
	 * @return true if the text is convertible to number. */
	public boolean isNum();

	/** Type check.
	 * @return true if the text is convertible to date. */
	public boolean isDate();

	// Unfortunately it seems impossible to implement Comparable<Object> since in this case
	// TypeTransformation.compareToWithEqualityCheck() is called and will return false for
	//   assert new Comparable(2) == "2"
	// instead of just calling equals, which is correctly defined
	public int compareTo(Object string);

	public int compareTo(Convertible convertible);

	/** parses the text (case insensitive) as boolean via {@link Boolean#parseBoolean(String)}.
	 * @return boolean */
	public boolean getBool();

	/** For implicit conversion to boolean: true if the text is not empty.
	 * @return boolean */
	public boolean asBoolean();
}
