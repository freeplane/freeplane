package org.freeplane.core.ui.components.html;

import java.awt.Color;
import java.awt.Font;

import javax.swing.SwingConstants;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.Quantity;

public class CssRuleBuilder {
	private StringBuilder rule = new StringBuilder();
	public String buildRule() {
		return rule.toString();
	}

	public CssRuleBuilder withFont(Font font) {
		return withFont(font, 1f);
	}

	public CssRuleBuilder withFont(Font font, float fontScaleFactor) {
		if (font != null) {
			rule.append(" font-family: \"");
			rule.append(font.getFamily());
			rule.append("\"; ");
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
		}
		return this;
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

	public CssRuleBuilder withMaxWidthAsPt(Quantity<?>... widths) {
		int maxBaseUnits = -1;
		for(Quantity<?> width : widths)
			if(width != null)
				maxBaseUnits = Math.max(maxBaseUnits, width.toBaseUnitsRounded());
		if(maxBaseUnits >= 0)
	   		rule.append("width: ")
			.append(maxBaseUnits).append("pt")
			.append(";");
 		return this;
	}
}
