package org.freeplane.features.common.format;

import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.FastDateFormat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;

/** a thin wrapper around {@link FastDateFormat} and {@link Formatter}.
 * <p>
 * Parsing is not supported! */
public abstract class PatternFormat /*extends Format*/ {
	private static final long serialVersionUID = 1L;
	static final String STYLE_FORMATTER = "formatter";
	static final String STYLE_DATE = "date";
	static final String TYPE_NUMBER = "number";
	static final String TYPE_DATE = "date";
	static final String TYPE_STRING = "string";
	private static final String ELEMENT_NAME = "format";
	private final String type;
	private final String pattern;

	public PatternFormat(String pattern, String type) {
		this.type = type;
		this.pattern = pattern;
	}

	/** the formal format description. */
	public String getPattern() {
		return pattern;
	}

	/** selects the kind of data the formatter is intended to format. */
	public String getType() {
		return type;
	}

	/** selects the formatter implementation, e.g. "formatter" or "date" */
	public abstract String getStyle();

	public static PatternFormat createPatternFormat(final String pattern, final String style, final String type) {
		if (style.equals(STYLE_DATE))
			return new DatePatternFormat(pattern, type);
		else if (style.equals(STYLE_FORMATTER))
			return new FormatterPatternFormat(pattern, type);
		else
			throw new IllegalArgumentException("unknown format style");
	}

	// yyyy-MM-dd HH:mm:ss
	final static Pattern datePattern = Pattern.compile("yyyy");

	// %[argument_index$] [flags] [width] conversion
	// == conversions
	// ignore boolean: bB
	// ignore hash: hH
	// sS
	// ignore char: cC
	// number: doxXeEfgGaA
	// ignore literals: %n
	// time prefix: tT
	final static Pattern formatterPattern = Pattern.compile("%" //
		// + "(?:[\\d<]+\\$)?" // Freeplane: no support for argument index$!
		+ "(?:[-#+ 0,(]+)?" // flags
		+ "(?:[\\d.]+)?" // width
		+ "([sSdoxXeEfgGaA]|[tT][HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc])"); // conversion

	public static PatternFormat guessPatternFormat(final String pattern) {
		try {
			final Matcher matcher = formatterPattern.matcher(pattern);
			if (matcher.find()) {
				// System.err.println("hi, pattern='" + pattern + "' match='" + matcher.group() + "'");
				final char conversion = matcher.group(1).charAt(0);
				if (matcher.find()) {
					LogUtils.warn("found multiple formats in this formatter pattern: '" + pattern + "'");
					return null;
				}
				switch (conversion) {
					case 's':
					case 'S':
						return new FormatterPatternFormat(pattern, TYPE_STRING);
					case 'd':
					case 'o':
					case 'x':
					case 'X':
					case 'e':
					case 'E':
					case 'f':
					case 'g':
					case 'G':
					case 'a':
					case 'A':
						return new FormatterPatternFormat(pattern, TYPE_NUMBER);
					case 't':
					case 'T':
						return new FormatterPatternFormat(pattern, TYPE_DATE);
				}
			}
			if (datePattern.matcher(pattern).find()) {
				return new DatePatternFormat(pattern, TYPE_DATE);
			}
			LogUtils.warn("not a pattern format: '" + pattern + "'");
			return null;
		}
		catch (Exception e) {
			LogUtils.warn("can't build a formatter for this pattern '" + pattern + "'", e);
			return null;
		}
	}

	public void toXml(XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(ELEMENT_NAME);
		child.setAttribute("type", getType());
		child.setAttribute("style", getStyle());
		child.setContent(getPattern());
		element.addChild(child);
	}

	public boolean acceptsDate() {
	    return getType().equals(TYPE_DATE);
    }
	
	public boolean acceptsNumber() {
		return getType().equals(TYPE_NUMBER);
	}
	
	public boolean acceptsString() {
		return getType().equals(TYPE_STRING);
	}

	abstract public Object format(Object toFormat);
}
