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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.web.UpdateCheckAction;

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

	final private WebDocuAction webDocu;

	public HelpController(final Controller controller) {
		super();
		controller.addAction(new AboutAction(controller));
		controller.addAction(new UpdateCheckAction(controller));
		controller.addAction(new OpenURLAction("OpenFreeplaneSiteAction", controller, ResourceController
		    .getResourceController().getProperty("webFreeplaneLocation")));
		controller.addAction(new FaqOpenURLAction(controller, ResourceController.getResourceController().getProperty(
		    "webFAQLocation")));
		controller.addAction(new KeyDocumentationAction(controller));
		webDocu = new WebDocuAction(controller, ResourceController.getResourceController().getProperty(
		    "webDocuLocation"));
		controller.addAction(webDocu);
		controller.addAction(new DocumentationAction(controller));
		controller.addAction(new LicenseAction(controller));
	}

	/**
	 * @param e
	 */
	public void webDocu(final ActionEvent e) {
		webDocu.actionPerformed(e);
	}
}
