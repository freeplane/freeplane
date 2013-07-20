package org.freeplane.features.format;

import org.freeplane.core.util.TextUtils;

public class IdentityPatternFormat extends PatternFormat {
	public IdentityPatternFormat(String pattern) {
		super(pattern, TYPE_IDENTITY);
	}
	
	IdentityPatternFormat() {
		this(IDENTITY_PATTERN);
	}

	@Override
	public String getStyle() {
		return STYLE_FORMATTER;
	}

    @Override
    public Object formatObject(Object toFormat) {
        if (toFormat instanceof IFormattedObject)
            return ((IFormattedObject) toFormat).getObject();
        return toFormat;
    }

    @Override
    public String toString() {
        return TextUtils.getText(getPattern());
    }
}