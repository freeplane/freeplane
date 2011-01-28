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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.NodeModel;

/**
 * @author Stefan Ott
 * 
 * This class has only one static method to show the editor for Latex-fomulas
 */
public class LatexEditor {
	/**
	 * This method shows the Latex editor and sets the equation to be rendered from Latex
	 * @param oldEquation: previous equation
	 * 
	 */
	public static String editLatex(final String oldEquation, final NodeModel node) {
		final JEditorPane textArea = new JEditorPane();
		final JScrollPane editorScrollPane = new JScrollPane(textArea);
		editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(700, 200));
		final JOptionPane editPane = new JOptionPane(editorScrollPane, JOptionPane.PLAIN_MESSAGE,
		    JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			//set initial focus to textArea
			@Override
			public void selectInitialValue() {
				textArea.requestFocusInWindow();
			}
		};
		final JDialog edit = editPane.createDialog(null, TextUtils.getText("plugins/latex/LatexNodeHook.editorTitle"));
		edit.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// set content and rendering for textArea
		textArea.setContentType("text/groovy"); /* text/groovy is from JSyntaxPane */
		textArea.setText(oldEquation);
		//bt is the OK button
		final JPanel jp = (JPanel) editPane.getComponent(3);
		final JButton bt = (JButton) jp.getComponent(0);
		//make Alt+ Enter confirm the dialog
		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getModifiers() == KeyEvent.ALT_MASK && e.getKeyChar() == KeyEvent.VK_ENTER) {
					bt.doClick();
				}
			}
		});
		//position editor below node
		Controller.getCurrentModeController().getController().getViewController().scrollNodeToVisible(node);
		if (ResourceController.getResourceController().getBooleanProperty("el__position_window_below_node")) {
			UITools.setDialogLocationUnder(edit, node);
		}
		edit.setVisible(true);
		if (editPane.getValue().equals(JOptionPane.OK_OPTION)) {
			final String eq = textArea.getText();
			return eq;
		}
		return null;
	}
}
