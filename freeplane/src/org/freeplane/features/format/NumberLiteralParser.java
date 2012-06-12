package org.freeplane.features.format;

public class NumberLiteralParser extends Parser {
	public NumberLiteralParser() {
		super(Parser.STYLE_NUMBERLITERAL, IFormattedObject.TYPE_NUMBER, null);
	}

	@Override
	Object parse(String string) {
		try {
			if (string == null || string.indexOf(' ') != -1)
				return null;
			final Double doubleValue = Double.valueOf(string);
			return new FormattedNumber(doubleValue);
		}
		catch (Exception e) {
			return null;
		}
	}
}
