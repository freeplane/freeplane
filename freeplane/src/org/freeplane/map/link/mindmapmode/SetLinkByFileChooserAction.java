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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.map.link.LinkController;
import org.freeplane.map.url.UrlManager;
import org.freeplane.map.url.mindmapmode.FileManager;
import org.freeplane.modes.mindmapmode.MModeController;

class SetLinkByFileChooserAction extends FreeplaneAction {
	public SetLinkByFileChooserAction() {
		super("set_link_by_filechooser");
	}

	public void actionPerformed(final ActionEvent e) {
		setLinkByFileChooser();
	}

	public void setLinkByFileChooser() {
		final String relative = ((FileManager) UrlManager.getController(getModeController()))
		    .getLinkByFileChooser(Controller.getController().getMap());
		if (relative != null) {
			((MLinkController) LinkController.getController(MModeController.getMModeController())).setLink(
			    MModeController.getMModeController().getSelectedNode(), relative);
		}
	}
}
