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
package org.freeplane.features.help;

import java.awt.event.ActionEvent;
import java.net.URL;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

class KeyDocumentationAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
// // 	final private Controller controller;

	public KeyDocumentationAction() {
		super("KeyDocumentationAction");
//		this.controller = controller;
	}

	public void actionPerformed(final ActionEvent e) {
		String urlText = TextUtils.getText("pdfKeyDocLocation");
		urlText = TextUtils.removeTranslateComment(urlText);
		try {
			if (urlText != null && urlText != "") {
				URL url = null;
				url = ResourceController.getResourceController().getResource(urlText);
				Controller.getCurrentController().getViewController().openDocument(url);
			}
		}
		catch (final Exception e2) {
			LogUtils.severe(e2);
			return;
		}
	}
}
