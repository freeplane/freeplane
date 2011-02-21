package org.freeplane.features.common.format;

import java.text.FieldPosition;

import org.apache.commons.lang.time.FastDateFormat;

class DatePatternFormat extends PatternFormat {
	private static final long serialVersionUID = 1L;
	private FastDateFormat fastDateFormat;

	public DatePatternFormat(String pattern, String type) {
        super(pattern, type);
        this.fastDateFormat = FastDateFormat.getInstance(pattern);
    }
	
	/**
	 * Formats an object via {@link FastDateFormat#format(Object)}.
	 * 
	 * @param obj  the object to format
	 * @param toAppendTo  the buffer to append to
	 * @param pos  the position - ignored
	 * @return the buffer passed in
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		return fastDateFormat.format(obj, toAppendTo, pos);
	}

	@Override
    public String getStyle() {
	    return PatternFormat.STYLE_DATE;
    }
}