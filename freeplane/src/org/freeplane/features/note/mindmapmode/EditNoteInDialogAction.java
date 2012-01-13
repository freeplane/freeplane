/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.note.mindmapmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeWYSIWYG;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;
import org.freeplane.features.ui.ViewController;

class EditNoteInDialogAction extends AFreeplaneAction {
	private static final Pattern HTML_HEAD = Pattern.compile("\\s*<head>.*</head>", Pattern.DOTALL);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EditNodeBase mCurrentEditDialog = null;

	public EditNoteInDialogAction() {
		super("EditNoteInDialogAction");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.actions.ActorXml#act(freeplane.controller.actions.
	 * generated.instance.XmlAction)
	 */
	public void actionPerformed(final ActionEvent arg0) {
		final Controller controller = Controller.getCurrentController();
		final NodeModel nodeModel = controller.getSelection().getSelected();
		final ViewController viewController = controller.getViewController();
		final Component node = viewController.getComponent(nodeModel);
		node.requestFocus();
		edit(nodeModel);
	}

	void edit(final NodeModel nodeModel) {
		final Controller controller = Controller.getCurrentController();
	    stopEditing();
		Controller.getCurrentModeController().setBlocked(true);
		String text = NoteModel.getNoteText(nodeModel);
		if(text ==  null){
			text = "";
		}
		final EditNodeWYSIWYG editNodeWYSIWYG = new EditNodeWYSIWYG("EditNoteInDialogAction.text", nodeModel, text, new EditNodeBase.IEditControl() {
			public void cancel() {
				Controller.getCurrentModeController().setBlocked(false);
				mCurrentEditDialog = null;
			}

			public void ok(final String newText) {
				setHtmlText(nodeModel, newText);
				cancel();
			}

			public void split(final String newText, final int position) {
			}
			public boolean canSplit() {
                return false;
            }

			public EditedComponent getEditType() {
                return EditedComponent.NOTE;
            }
		}, false);
		mCurrentEditDialog = editNodeWYSIWYG;
		editNodeWYSIWYG.setBackground(Color.WHITE);
		// set default font for notes:
		final NodeStyleController style = (NodeStyleController) Controller.getCurrentModeController().getExtension(
		    NodeStyleController.class);
		MapModel map = Controller.getCurrentModeController().getController().getMap();
		if(map != null){
		    final Font defaultFont = style.getDefaultFont(map, MapStyleModel.NOTE_STYLE);
		    editNodeWYSIWYG.setFont(defaultFont);
		}
		final RootPaneContainer frame = (RootPaneContainer) SwingUtilities.getWindowAncestor(controller.getViewController().getMapView());
		editNodeWYSIWYG.show(frame);
    }


	private void setHtmlText(final NodeModel node, final String newText) {
		final String body = EditNoteInDialogAction.HTML_HEAD.matcher(newText).replaceFirst("");
		final MNoteController noteController = (MNoteController) MNoteController.getController();
		noteController.setNoteText(node, body.replaceFirst("\\s+$", ""));
	}

	private void stopEditing() {
		if (mCurrentEditDialog != null) {
			mCurrentEditDialog.closeEdit();
			mCurrentEditDialog = null;
		}
	}
}
