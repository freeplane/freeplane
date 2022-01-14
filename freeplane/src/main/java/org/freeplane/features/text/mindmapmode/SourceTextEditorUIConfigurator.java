package org.freeplane.features.text.mindmapmode;

import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;

public class SourceTextEditorUIConfigurator {
	public static void configureColors(JTextComponent textEditor) {
		textEditor.setOpaque(true);
		textEditor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
	}
}
