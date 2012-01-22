package org.freeplane.features.format;

import org.freeplane.core.util.TextUtils;

class IdentityPatternFormat extends PatternFormat {
	private static final String NAME = TextUtils.getText(IDENTITY_PATTERN);

    public IdentityPatternFormat() {
		super(IDENTITY_PATTERN, TYPE_IDENTITY);
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
        return NAME;
    }
}