package org.freeplane.features.common.format;

import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;
import org.freeplane.core.util.FreeplaneDate;

class DatePatternFormat extends PatternFormat {
	private static final long serialVersionUID = 1L;
	public DatePatternFormat(String pattern, String type) {
        super(pattern, type);
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
	public Object format(Object obj) {
		if(obj instanceof Date)
			return new FreeplaneDate((Date)obj, getPattern());
		return obj;
	}

	@Override
    public String getStyle() {
	    return PatternFormat.STYLE_DATE;
    }
}