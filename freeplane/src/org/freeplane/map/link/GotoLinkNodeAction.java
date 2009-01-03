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
package org.freeplane.map.link;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.FreeplaneAction;

/** Follow a graphical link (AKA connector) action. */
class GotoLinkNodeAction extends FreeplaneAction {
	NodeModel source;

	public GotoLinkNodeAction(final LinkController linkController, final NodeModel source) {
		super("goto_link_node_action", "images/Link.png");
		this.source = source;
		if (source != null) {
			final String adaptedText = source.getShortText(linkController.getModeController());
			putValue(Action.NAME, linkController.getModeController().getText(
			    "follow_graphical_link")
			        + adaptedText);
			putValue(Action.SHORT_DESCRIPTION, source.toString());
		}
	}

	public void actionPerformed(final ActionEvent e) {
		getModeController().getMapController().centerNode(source);
	}
}
