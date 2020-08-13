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
package org.freeplane.features.text;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;


@SelectableAction(checkOnNodeChange=true)
class SetShortenerStateAction extends AMultipleNodeAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private boolean setShortened;
	public SetShortenerStateAction() {
		super("SetShortenerStateAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		setShortened = !isShortened();
		final Controller controller = Controller.getCurrentController();
		final IMapSelection selection = controller.getSelection();
		final NodeModel node = selection.getSelected();
		controller.getMapViewManager().getComponent(node).requestFocusInWindow();
		selection.preserveRootNodeLocationOnScreen();
		super.actionPerformed(e);
	}

	private boolean isShortened() {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if(node == null){
			return false;
		}
		final ShortenedTextModel model = ShortenedTextModel.getShortenedTextModel(node);
		return model != null;
    }

	@Override
    protected void actionPerformed(ActionEvent e, NodeModel node) {
		TextController controller = TextController.getController();
		controller.setIsMinimized(node, setShortened);
    }

	@Override
	public void setSelected() {
		setSelected(isShortened());
	}

}
