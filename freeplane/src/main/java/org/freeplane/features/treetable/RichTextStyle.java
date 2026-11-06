package org.freeplane.features.treetable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RichTextStyle {
	public static final RichTextStyle PLAIN = new RichTextStyle(false, false, false, false, null);

	private static final int BOLD_WEIGHT = 600;
	private static final Pattern SHORT_HEX_COLOR = Pattern.compile("#([0-9a-f])([0-9a-f])([0-9a-f])");
	private static final Pattern HEX_COLOR = Pattern.compile("#[0-9a-f]{6}");
	private static final Pattern RGB_COLOR = Pattern.compile("rgb\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
	private static final Map<String, String> NAMED_COLORS = new HashMap<String, String>();
	static {
		NAMED_COLORS.put("red", "#ff0000");
		NAMED_COLORS.put("green", "#008000");
		NAMED_COLORS.put("lime", "#00ff00");
		NAMED_COLORS.put("blue", "#0000ff");
		NAMED_COLORS.put("navy", "#000080");
		NAMED_COLORS.put("yellow", "#ffff00");
		NAMED_COLORS.put("orange", "#ffa500");
		NAMED_COLORS.put("purple", "#800080");
		NAMED_COLORS.put("fuchsia", "#ff00ff");
		NAMED_COLORS.put("aqua", "#00ffff");
		NAMED_COLORS.put("teal", "#008080");
		NAMED_COLORS.put("maroon", "#800000");
		NAMED_COLORS.put("olive", "#808000");
		NAMED_COLORS.put("gray", "#808080");
		NAMED_COLORS.put("grey", "#808080");
		NAMED_COLORS.put("silver", "#c0c0c0");
		NAMED_COLORS.put("white", "#ffffff");
	}

	private final boolean bold;
	private final boolean italic;
	private final boolean underline;
	private final boolean strikethrough;
	private final String color;

	private RichTextStyle(final boolean bold, final boolean italic, final boolean underline,
			final boolean strikethrough, final String color) {
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.strikethrough = strikethrough;
		this.color = color;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public boolean isUnderline() {
		return underline;
	}

	public boolean isStrikethrough() {
		return strikethrough;
	}

	public String getColor() {
		return color;
	}

	public boolean isPlain() {
		return equals(PLAIN);
	}

	public RichTextStyle withBold(final boolean bold) {
		return new RichTextStyle(bold, italic, underline, strikethrough, color);
	}

	public RichTextStyle withItalic(final boolean italic) {
		return new RichTextStyle(bold, italic, underline, strikethrough, color);
	}

	public RichTextStyle withUnderline(final boolean underline) {
		return new RichTextStyle(bold, italic, underline, strikethrough, color);
	}

	public RichTextStyle withStrikethrough(final boolean strikethrough) {
		return new RichTextStyle(bold, italic, underline, strikethrough, color);
	}

	public RichTextStyle withColor(final String color) {
		return new RichTextStyle(bold, italic, underline, strikethrough, normalizedColor(color));
	}

	public RichTextStyle withCss(final String declarations) {
		RichTextStyle style = this;
		if (declarations == null)
			return style;
		for (final String declaration : declarations.split(";")) {
			final int colon = declaration.indexOf(':');
			if (colon > 0)
				style = style.withCssProperty(declaration.substring(0, colon).trim().toLowerCase(Locale.ENGLISH),
					declaration.substring(colon + 1).trim().toLowerCase(Locale.ENGLISH));
		}
		return style;
	}

	private RichTextStyle withCssProperty(final String property, final String value) {
		switch (property) {
		case "font-weight":
			return withBold(isBoldWeight(value));
		case "font-style":
			return withItalic(value.contains("italic") || value.contains("oblique"));
		case "text-decoration":
		case "text-decoration-line":
			if (value.contains("none"))
				return withUnderline(false).withStrikethrough(false);
			return withUnderline(underline || value.contains("underline"))
				.withStrikethrough(strikethrough || value.contains("line-through"));
		case "text-underline-style":
			return withUnderline(!value.contains("none"));
		case "color":
			return withColor(value);
		default:
			return this;
		}
	}

	private static boolean isBoldWeight(final String value) {
		if (value.startsWith("bold"))
			return true;
		try {
			return Integer.parseInt(value) >= BOLD_WEIGHT;
		}
		catch (final NumberFormatException e) {
			return false;
		}
	}

	static String normalizedColor(final String color) {
		if (color == null)
			return null;
		final String value = color.trim().replace("!important", "").trim().toLowerCase(Locale.ENGLISH);
		final Matcher shortHex = SHORT_HEX_COLOR.matcher(value);
		if (shortHex.matches())
			return "#" + shortHex.group(1) + shortHex.group(1) + shortHex.group(2) + shortHex.group(2)
					+ shortHex.group(3) + shortHex.group(3);
		if (HEX_COLOR.matcher(value).matches())
			return "#000000".equals(value) ? null : value;
		final Matcher rgb = RGB_COLOR.matcher(value);
		if (rgb.matches()) {
			final String hex = String.format("#%02x%02x%02x", Integer.parseInt(rgb.group(1)),
				Integer.parseInt(rgb.group(2)), Integer.parseInt(rgb.group(3)));
			return "#000000".equals(hex) ? null : hex;
		}
		return NAMED_COLORS.get(value);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof RichTextStyle))
			return false;
		final RichTextStyle other = (RichTextStyle) obj;
		return bold == other.bold && italic == other.italic && underline == other.underline
				&& strikethrough == other.strikethrough && Objects.equals(color, other.color);
	}

	@Override
	public int hashCode() {
		return Objects.hash(bold, italic, underline, strikethrough, color);
	}

	@Override
	public String toString() {
		return (bold ? "b" : "") + (italic ? "i" : "") + (underline ? "u" : "") + (strikethrough ? "s" : "")
				+ (color != null ? color : "");
	}
}
