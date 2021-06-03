package org.freeplane.features.text.mindmapmode;

import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;

public class SourceTextEditorUIConfigurator {
	public static void configureColors(JTextComponent textEditor) {
		textEditor.setOpaque(true);
		textEditor.setBackground(Color.WHITE);
		textEditor.setForeground(Color.BLACK);
		textEditor.setSelectionColor(Color.BLUE.darker());
		textEditor.setSelectedTextColor(Color.WHITE);
		textEditor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
	}
}
