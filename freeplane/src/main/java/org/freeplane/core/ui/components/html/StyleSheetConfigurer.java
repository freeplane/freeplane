package org.freeplane.core.ui.components.html;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.text.StyleContext;
import javax.swing.text.html.StyleSheet;

public class StyleSheetConfigurer {
    public static void resetStyles(StyleSheet parent,  int keptChildCount) {
        Enumeration<?> styleNamesEnumeration = parent.getStyleNames();
        ArrayList<String> styleNames = new ArrayList<String>();
        while(styleNamesEnumeration.hasMoreElements()) {
            String styleName = (String) styleNamesEnumeration.nextElement();
            if (!styleName.equalsIgnoreCase(StyleContext.DEFAULT_STYLE))
                styleNames.add(styleName);
        }

        for(String styleName : styleNames) {
            parent.removeStyle(styleName);
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
