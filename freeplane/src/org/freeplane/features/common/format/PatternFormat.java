package org.freeplane.features.common.format;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Formatter;

import org.apache.commons.lang.time.FastDateFormat;
import org.freeplane.n3.nanoxml.XMLElement;

/** a thin wrapper around {@link FastDateFormat} and {@link Formatter}.
 * <p>
 * Parsing is not supported! */
public abstract class PatternFormat extends Format {
	private static final long serialVersionUID = 1L;
	static final String STYLE_FORMATTER = "formatter";
	static final String STYLE_DATE = "date";
	static final String TYPE_NUMBER = "number";
	static final String TYPE_DATE = "date";
	private static final String ELEMENT_NAME = "format";
	private final String type;
	private final String pattern;

	public PatternFormat(String pattern, String type) {
		this.type = type;
		this.pattern = pattern;
	}

	/** parsing is not supported. */
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		pos.setIndex(0);
		pos.setErrorIndex(0);
		return null;
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

	public static PatternFormat createPatternFormat(String pattern, String style, String type) {
		if (style.equals(STYLE_DATE))
			return new DatePatternFormat(pattern, type);
		else if (style.equals(STYLE_FORMATTER))
			return new FormatterPatternFormat(pattern, type);
		else
			throw new IllegalArgumentException("unknown format style");
	}

	public void toXml(XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(ELEMENT_NAME);
		child.setAttribute("type", getType());
		child.setAttribute("style", getStyle());
		child.setContent(getPattern());
		element.addChild(child);
	}
}
