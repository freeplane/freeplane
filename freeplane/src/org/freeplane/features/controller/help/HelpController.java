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
package org.freeplane.features.controller.help;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;

/**
 * @author Dimitry Polivaev
 */
public class HelpController implements IExtension {
	public static HelpController getController(final Controller controller) {
		return (HelpController) controller.getExtension(HelpController.class);
	}

	public static void install(final Controller controller) {
		controller.addExtension(HelpController.class, new HelpController(controller));
	}

	final private Action webDocu;

	public HelpController(final Controller controller) {
		super();
		controller.addAction("about", new AboutAction(controller));
		controller.addAction("freeplaneUrl", new OpenURLAction(controller, Controller.getResourceController().getText(
		    "Freeplane"), Controller.getResourceController().getProperty("webFreeplaneLocation")));
		controller.addAction("faq", new OpenURLAction(controller, Controller.getResourceController().getText("FAQ"),
		    Controller.getResourceController().getProperty("webFAQLocation")));
		controller.addAction("keyDocumentation", new KeyDocumentationAction(controller));
		webDocu = new OpenURLAction(controller, Controller.getResourceController().getText("webDocu"), Controller
		    .getResourceController().getProperty("webDocuLocation"));
		controller.addAction("webDocu", webDocu);
		controller.addAction("documentation", new DocumentationAction(controller));
		controller.addAction("license", new LicenseAction(controller));
	}

	/**
	 * @param e
	 */
	public void webDocu(final ActionEvent e) {
		webDocu.actionPerformed(e);
	}
}
