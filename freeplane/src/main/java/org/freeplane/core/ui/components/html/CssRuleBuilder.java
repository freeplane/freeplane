package org.freeplane.core.ui.components.html;

import java.awt.Color;
import java.awt.Font;

import javax.swing.SwingConstants;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.nodestyle.FontUtils;

public class CssRuleBuilder {
	private StringBuilder rule = new StringBuilder();
	public String buildRule() {
		return rule.toString();
	}

	public CssRuleBuilder withHTMLFont(Font font) {
		return withHTMLFont(font, 1f);
	}

	public CssRuleBuilder withCSSFont(Font font) {
		return withCSSFont(font, 1f);
	}

	public CssRuleBuilder withHTMLFont(Font font, float fontScaleFactor) {
		if (font != null) {
			withFontFamily(font.getFamily(), "&quot;");
			withFontConfiguration(font, fontScaleFactor);
		}
		return this;
	}

	public CssRuleBuilder withCSSFont(Font font, float fontScaleFactor) {
		if (font != null) {
			withFontFamily(font.getFamily(), "\"");
			withFontConfiguration(font, fontScaleFactor);
		}
		return this;
	}

	private void withFontConfiguration(Font font, float fontScaleFactor) {
		rule.append(" font-size: ");
		final int fontSize = Math.round(font.getSize() / fontScaleFactor);
		rule.append(fontSize);
		rule.append("pt;");
		if (font.isBold()) {
			rule.append(" font-weight: bold;");
		}
		if (font.isItalic()) {
			rule.append(" font-style: italic;");
		}
		if(FontUtils.isStrikedThrough(font)) {
			rule.append(" text-decoration: line-through;");
		}
	}

	private void withFontFamily(String family, String quote) {
		rule.append(" font-family: ").append(quote).append(family).append(quote).append("; ");
	}

	public CssRuleBuilder withColor(Color color) {
		if (color != null) {
			rule.append("color: ").append(ColorUtils.colorToString(color)).append(";");
		}
		return this;
	}

	public CssRuleBuilder withBackground(Color color) {
		if (color != null) {
			rule.append("background-color: ").append(ColorUtils.colorToString(color)).append(";");
		}
		return this;
	}

	public CssRuleBuilder withAlignment(int alignment) {
		switch (alignment) {
		case SwingConstants.CENTER:
			rule.append("text-align: center;");
			break;
		case SwingConstants.LEFT:
			rule.append("text-align: left;");
			break;
		case SwingConstants.RIGHT:
			rule.append("text-align: right;");
			break;
		default:
			break;
		}
		return this;

	}

	public String toString() {
		return rule.toString();
	}

	public CssRuleBuilder withMaxWidthAsPt(float zoom, Quantity<?>... widths) {
		int maxBaseUnits = -1;
		for(Quantity<?> width : widths)
			if(width != null) {
				final int zoomedBaseUnits = (int) (zoom * width.toBaseUnits() + 0.5d);
				maxBaseUnits = Math.max(maxBaseUnits, zoomedBaseUnits);
			}
		if(maxBaseUnits >= 0)
	   		rule.append("width: ")
			.append(maxBaseUnits).append("pt")
			.append(";");
 		return this;
	}
}
