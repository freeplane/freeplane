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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Stefan Ott
 * 
 * This class has only one static method to show the editor for _legacy_ Latex-fomulas
 * (deprecated!)
 */
public class LegacyLatexEditor {
	@SuppressWarnings("serial")
    private static final class DialogCloser extends AbstractAction {
	    private JDialog dialog;
	    private boolean closed = false;

		boolean isClosed() {
        	return closed;
        }

		public DialogCloser(JDialog dialog) {
	        this.dialog = dialog;
        }

		public void actionPerformed(ActionEvent e) {
			closed = true;
			dialog.dispose();
        }
    }

	/**
	 * This method shows the Latex editor and sets the equation to be rendered from Latex
	 * 
	 * @param oldEquation: previous equation
	 * @param node: the node that is edited (is used to position editor window)
	 * 
	 */
	public static String editLatex(final String oldEquation, final NodeModel node) {
		final JEditorPane textArea = new JEditorPane();
		textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
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
		//make Alt+ Enter confirm the dialog
		final DialogCloser dialogCloser = new DialogCloser(edit);
		textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_MASK, false), dialogCloser);
		textArea.getActionMap().put(dialogCloser, dialogCloser);
		//position editor below node
		Controller.getCurrentModeController().getController().getMapViewManager().scrollNodeToVisible(node);
		if (ResourceController.getResourceController().getBooleanProperty("el__position_window_below_node")) {
			UITools.setDialogLocationUnder(edit, node);
		}
		edit.setVisible(true);
		if (dialogCloser.isClosed() || editPane.getValue().equals(JOptionPane.OK_OPTION)) {
			final String eq = textArea.getText();
			return eq;
		}
		return null;
	}
}
