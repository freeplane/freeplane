package org.freeplane.features.nodestyle;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

public class FontUtils {
	@SuppressWarnings("unchecked")
	public static Font strikeThrough(final Font font) {
		@SuppressWarnings("rawtypes")
		final Map attributes = font.getAttributes();
		attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		Font newFont = new Font(attributes);
		return newFont;
	}

	public static boolean isStrikedThrough(final Font font) {
		return TextAttribute.STRIKETHROUGH_ON.equals(font.getAttributes().get(TextAttribute.STRIKETHROUGH));
	}
}