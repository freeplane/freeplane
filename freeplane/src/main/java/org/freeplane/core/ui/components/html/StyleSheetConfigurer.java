package org.freeplane.core.ui.components.html;

import java.util.Enumeration;

import javax.swing.text.StyleContext;
import javax.swing.text.html.StyleSheet;

public class StyleSheetConfigurer {
	public static void resetStyles(StyleSheet parent,  int keptChildCount) {
		Enumeration<?> styleNames = parent.getStyleNames();
		while(styleNames.hasMoreElements()) {
			String ruleName = (String) styleNames.nextElement();
			 if (!ruleName.equalsIgnoreCase(StyleContext.DEFAULT_STYLE))
				 parent.removeStyle(ruleName);
		}
		StyleSheet[] styleSheets = parent.getStyleSheets();
		if(styleSheets != null) {
			int removedSheetsCount = styleSheets.length - keptChildCount;
			for(int i = 0; i < removedSheetsCount; i++ ) {
				parent.removeStyleSheet(styleSheets[i]);
			}
		}
	}
	
	public static StyleSheet createDefaultStyleSheet() {
		StyleSheet styleSheet = new StyleSheet();
		styleSheet.addRule("table {border: 0; border-spacing: 0;}");
		styleSheet.addRule("th, td {border: 1px solid;}");
		return styleSheet;
	}
}
