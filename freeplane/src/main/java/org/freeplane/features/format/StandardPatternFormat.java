package org.freeplane.features.format;

import org.freeplane.core.util.TextUtils;

class StandardPatternFormat extends PatternFormat {
	public StandardPatternFormat() {
		super(STANDARD_FORMAT_PATTERN, TYPE_STANDARD);
	}

	@Override
	public String getStyle() {
		return STYLE_FORMATTER;
	}

    @Override
    public Object formatObject(Object toFormat) {
        if (toFormat instanceof IFormattedObject)
            toFormat = ((IFormattedObject) toFormat).getObject();
        return FormatController.formatUsingDefault(toFormat);
    }

    @Override
    public String toString() {
        return TextUtils.getText(STANDARD_FORMAT_PATTERN);
    }
}