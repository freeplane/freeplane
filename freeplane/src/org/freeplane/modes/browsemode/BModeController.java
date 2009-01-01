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
package org.freeplane.modes.browsemode;

import java.awt.event.MouseEvent;
import java.util.ListIterator;

import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.map.note.NodeNoteBase;
import org.freeplane.view.map.MainView;

public class BModeController extends ModeController {
	static public final String MODENAME = "Browse";
	private ImageIcon noteIcon;

	BModeController() {
		super();
	}

	public void doubleClick() {
		/* If the link exists, follow the link; toggle folded otherwise */
		if (getSelectedNode().getLink() == null) {
			getMapController().toggleFolded();
		}
		else {
			getLinkController().loadURL();
		}
	}

	@Override
	public String getModeName() {
		return BModeController.MODENAME;
	}

	@Override
	public void plainClick(final MouseEvent e) {
		/* perform action only if one selected node. */
		if (getSelectedNodes().size() != 1) {
			return;
		}
		final MainView component = (MainView) e.getComponent();
		if (component.isInFollowLinkRegion(e.getX())) {
			getLinkController().loadURL();
		}
		else {
			final NodeModel node = (component).getNodeView().getModel();
			if (!getMapController().hasChildren(node)) {
				doubleClick(e);
				return;
			}
			getMapController().toggleFolded(getSelectedNodes().listIterator());
		}
	}

	public void setNoteIcon(final NodeModel node) {
		final String noteText = node.getNoteText();
		if (noteText != null && !noteText.equals("")) {
			if (noteIcon == null) {
				noteIcon = new ImageIcon(Controller.getResourceController().getResource(
				    "images/knotes.png"));
			}
			node.setStateIcon(NodeNoteBase.NODE_NOTE_ICON, noteIcon);
		}
		final ListIterator children = node.getModeController().getMapController().childrenUnfolded(
		    node);
		while (children.hasNext()) {
			setNoteIcon((NodeModel) children.next());
		}
	}

	@Override
	protected void updateMenus(final String resource) {
		super.updateMenus(resource);
	}
}
