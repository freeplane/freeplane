package org.freeplane.features.common.format;

import java.util.Date;

import org.freeplane.core.util.FreeplaneDate;

class DatePatternFormat extends PatternFormat {
	private static final long serialVersionUID = 1L;
	public DatePatternFormat(String pattern) {
        super(pattern, PatternFormat.TYPE_DATE);
    }
	
	@Override
	public Object formatObject(Object obj) {
		if(obj instanceof Date)
			return new FreeplaneDate((Date)obj, getPattern());
		return obj;
	}

	@Override
    public String getStyle() {
	    return PatternFormat.STYLE_DATE;
    }
}