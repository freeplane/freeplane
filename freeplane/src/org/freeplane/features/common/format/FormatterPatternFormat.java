package org.freeplane.features.common.format;

import java.text.FieldPosition;
import java.util.Formatter;

class FormatterPatternFormat extends PatternFormat {
	private static final long serialVersionUID = 1L;
	private final Formatter formatter;
	
	public FormatterPatternFormat(String pattern, String type) {
		super(pattern, type);
		this.formatter = new Formatter();
	}

	@Override
	public final String formatObject (Object obj) {
		return formatter.format(getPattern(), obj).toString();
	}
	/**
	 * Formats an object via {@link Formatter#format(String, Object...)}.
	 * 
	 * @param obj  the object to format
	 * @param toAppendTo  the buffer to append to
	 * @param pos  the position - ignored
	 * @return the buffer passed in
	 */
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		final String formatted = formatObject(obj);
		toAppendTo.append(formatted);
		return toAppendTo;
	}

	@Override
    public String getStyle() {
	    return PatternFormat.STYLE_FORMATTER;
    }
}