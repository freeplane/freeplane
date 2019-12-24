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
package org.freeplane.features.url.mindmapmode;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.FreeplaneUriConverter;

class OpenURLMapAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public OpenURLMapAction() {
		super("OpenURLMapAction");
	}

	public void actionPerformed(final ActionEvent e) {
		Controller.getCurrentController().selectMode(MModeController.MODENAME);
		String urlText = JOptionPane.showInputDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(),
			TextUtils.getText("enter_map_url"), "http://");
		if(urlText != null){
			try {
				String fixedUri = new FreeplaneUriConverter().fixPartiallyDecodedFreeplaneUriComingFromInternetExplorer(urlText);
				URI uri = new URI(fixedUri);
				LinkController.getController().loadURI(uri);
			}
			catch (Exception ex) {
				UITools.errorMessage(TextUtils.format("url_open_error", urlText));
				LogUtils.warn("can not load " + urlText, ex);
			}
		}
	}

	@Override
	public void afterMapChange(UserRole userRole, boolean isMapSelected) {
	}
}
