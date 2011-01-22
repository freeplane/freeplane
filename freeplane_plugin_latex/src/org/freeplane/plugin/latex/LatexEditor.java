/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Stefan Ott in 2010.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.plugin.latex;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

/**
 * @author Stefan Ott
 * 
 * This class has only one static method to show the editor for Latex-fomulas
 */
public class LatexEditor {
	/**
	 * This method shows the Latex editor and sets the equation to be rendered from Latex
	 * @param oldEquation TODO
	 * 
	 * @param nodeHook: reference to the node
	 * @param latexExtension: the latexExtension
	 */
	public static String editLatex(String oldEquation) {
		final JEditorPane textArea = new JEditorPane();
		final JScrollPane editorScrollPane = new JScrollPane(textArea);
		editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(700, 200));
		final JOptionPane editPane = new JOptionPane(editorScrollPane, JOptionPane.PLAIN_MESSAGE,
		    JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				textArea.requestFocusInWindow(); // (not pPanel)
			}
		};
		final JDialog edit = editPane.createDialog(null, LatexViewer.editorTitle);
		edit.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// set content and rendering for textArea
		textArea.setContentType("text/groovy"); /* text/groovy is from JSyntaxPane */
		textArea.setText(oldEquation);
		editorScrollPane.requestFocusInWindow();
		edit.setVisible(true);
		if (editPane.getValue().equals(JOptionPane.OK_OPTION)) {
			final String eq = textArea.getText();
			return eq;
		}
		return null;
	}
}
