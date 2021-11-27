package org.freeplane.core.ui.components.html;

import javax.swing.text.html.StyleSheet;

public class StyleSheetConfigurer {
	public static void resetLinkedStyleSheets(StyleSheet parent,  int keptChildCount) {
		StyleSheet[] styleSheets = parent.getStyleSheets();
		if(styleSheets != null) {
			int removedSheetsCount = styleSheets.length - keptChildCount;
			for(int i = 0; i < removedSheetsCount; i++ ) {
				parent.removeStyleSheet(styleSheets[i]);
			}
		}
	}
}
