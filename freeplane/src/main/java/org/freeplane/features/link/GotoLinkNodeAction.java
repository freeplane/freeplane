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
package org.freeplane.features.link;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;

/** Follow a graphical link (AKA connector) action. */
class GotoLinkNodeAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final LinkController linkController;
	private final NodeModel target;

	public GotoLinkNodeAction(final LinkController linkController, String actionName, final NodeModel target) {
	    this(linkController, target);
	}

	public GotoLinkNodeAction(final LinkController linkController, final NodeModel target) {
		super("GotoLinkNodeAction");
		this.target = target;
		this.linkController = linkController;
	}

	public void configureText(String actionName, final NodeModel target) {
	    if (target != null) {
			final String adaptedText = TextController.getController().getShortPlainText(target);
			putValue(Action.NAME, TextUtils.format(actionName, adaptedText));
			StringBuilder path = new StringBuilder();
			appendPath(path, target.getParentNode());
			path.append(target);
			putValue(Action.SHORT_DESCRIPTION, path.toString());
		}
    }

	private void appendPath(StringBuilder path, final NodeModel target) {
		if(target != null){
			appendPath(path, target.getParentNode());
			final String shortText = TextController.getController().getShortPlainText(target);
			path.append(shortText);
			path.append(" -> ");
		}
    }

	public void actionPerformed(final ActionEvent e) {
		linkController.onDeselect(Controller.getCurrentModeController().getMapController().getSelectedNode());
		Controller.getCurrentModeController().getMapController().select(target);
		linkController.onSelect(target);
	}
}
