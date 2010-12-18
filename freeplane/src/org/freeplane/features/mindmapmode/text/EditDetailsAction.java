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
package org.freeplane.features.mindmapmode.text;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.DetailTextModel;

class EditDetailsAction extends AFreeplaneAction {
	private static final Pattern HTML_HEAD = Pattern.compile("\\s*<head>.*</head>", Pattern.DOTALL);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EditNodeBase mCurrentEditDialog = null;

	public EditDetailsAction() {
		super("EditDetailsAction");
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
		String text = DetailTextModel.getDetailTextText(nodeModel);
		if(text ==  null){
			text = "";
		}
		KeyEvent firstEvent= null;
		final EditNodeWYSIWYG editNodeWYSIWYG = new EditNodeWYSIWYG(DetailTextModel.EDITING_PURPOSE, nodeModel, text, firstEvent, new EditNodeBase.IEditControl() {
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
		}, false);
		mCurrentEditDialog = editNodeWYSIWYG;
		editNodeWYSIWYG.show(controller.getViewController().getFrame());
    }


	private void setHtmlText(final NodeModel node, final String newText) {
		final String body = EditDetailsAction.HTML_HEAD.matcher(newText).replaceFirst("");
		final MTextController textController = (MTextController) MTextController.getController();
        textController.setDetails(node, body.replaceFirst("\\s+$", ""));
	}

	private void stopEditing() {
		if (mCurrentEditDialog != null) {
			mCurrentEditDialog.closeEdit();
			mCurrentEditDialog = null;
		}
	}
}
