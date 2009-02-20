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
package org.freeplane.features.controller.help;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;

import org.freeplane.core.actions.IFreeplaneAction;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogTool;

class KeyDocumentationAction extends AbstractAction implements IFreeplaneAction{
	private static final String NAME = "keyDocumentation";
    private static final long serialVersionUID = 3000552480373959869L;
	final private Controller controller;

	public KeyDocumentationAction(final Controller controller) {
		super();
		this.controller = controller;
		MenuBuilder.setLabelAndMnemonic(this, ResourceController.getText("KeyDoc"));
	}

	public void actionPerformed(final ActionEvent e) {
		String urlText = ResourceController.getText("pdfKeyDocLocation");
		urlText = ResourceController.removeTranslateComment(urlText);
		try {
			if (urlText != null && urlText != "") {
				URL url = null;
				url = ResourceController.getResourceController().getResource(urlText);
				controller.getViewController().openDocument(url);
			}
		}
		catch (final Exception e2) {
			LogTool.logException(e2);
			return;
		}
	}

	public String getName() {
	    return NAME;
    }
}
