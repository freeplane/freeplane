/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.controller.help;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.controller.Controller;
import org.freeplane.controller.Freeplane;

/**
 * @author Dimitry Polivaev
 */
public class HelpController {
	final private Action webDocu;

	public HelpController() {
		super();
		final Controller controller = Freeplane.getController();
		controller.addAction("about", new AboutAction());
		controller.addAction("freemindUrl", new OpenURLAction(controller
		    .getResourceController().getText("Freeplane"), controller
		    .getResourceController().getProperty("webFreeMindLocation")));
		controller.addAction("faq", new OpenURLAction(controller
		    .getResourceController().getText("FAQ"), controller
		    .getResourceController().getProperty("webFAQLocation")));
		controller.addAction("keyDocumentation", new KeyDocumentationAction());
		webDocu = new OpenURLAction(controller.getResourceController().getText(
		    "webDocu"), controller.getResourceController().getProperty(
		    "webDocuLocation"));
		controller.addAction("webDocu", webDocu);
		controller.addAction("documentation", new DocumentationAction());
		controller.addAction("license", new LicenseAction());
	}

	/** Used for MAC!!! */
	public String convertLocalLink(final String map) {
		/* new handling for relative urls. fc, 29.10.2003. */
		final String applicationPath = Freeplane.getController()
		    .getResourceController().getFreemindBaseDir();
		return "file:" + applicationPath + map.substring(1);
		/* end: new handling for relative urls. fc, 29.10.2003. */
	}

	/**
	 * @param e
	 */
	public void webDocu(final ActionEvent e) {
		webDocu.actionPerformed(e);
	}
}
