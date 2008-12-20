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
package org.freeplane.map.link.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.map.link.NodeLinks;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.undo.IUndoableActor;

class SetLinkByTextFieldAction extends FreeplaneAction {
	public SetLinkByTextFieldAction() {
		super("set_link_by_textfield", (String) null);
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController controller = getModeController();
		String inputValue = JOptionPane.showInputDialog(controller.getMapView().getSelected(),
		    Controller.getText("edit_link_manually"), controller.getSelectedNode().getLink());
		if (inputValue != null) {
			if (inputValue.equals("")) {
				inputValue = null;
			}
			setLink(controller.getSelectedNode(), inputValue);
		}
	}

	public void setLink(final NodeModel node, final String link) {
		final IUndoableActor actor = new IUndoableActor() {
			private String oldlink;
			private String oldTargetID;

			public void act() {
				NodeLinks links = NodeLinks.getLinkExtension(node);
				if (links != null) {
					oldlink = links.getLink();
					oldTargetID = links.removeLocalHyperLink();
				}
				else {
					links = NodeLinks.createLinkExtension(node);
				}
				if (link != null && link.startsWith("#")) {
					links.setLocalHyperlink(link.substring(1));
				}
				links.setLink(link);
				getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setLink";
			}

			public void undo() {
				final NodeLinks links = NodeLinks.getLinkExtension(node);
				links.setLocalHyperlink(oldTargetID);
				links.setLink(oldlink);
				getModeController().getMapController().nodeChanged(node);
			}
		};
		getMModeController().execute(actor);
	}
}
