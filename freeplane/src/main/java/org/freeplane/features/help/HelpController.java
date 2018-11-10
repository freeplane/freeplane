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
package org.freeplane.features.help;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
public class HelpController implements IExtension {
	public static HelpController getController() {
		return Controller.getCurrentController().getExtension(HelpController.class);
	}

	public static void install() {
		Controller controller = Controller.getCurrentController();
		controller.addExtension(HelpController.class, new HelpController());
	}

	public HelpController() {
		super();
		Controller controller = Controller.getCurrentController();
        final ResourceController resourceController = ResourceController.getResourceController();
		controller.addAction(new AboutAction());
        controller.addAction(new OpenURLAction("OpenFreeplaneSiteAction",  resourceController.getProperty("webFreeplaneLocation")));
        if(resourceController.isApplet())
            return;
		controller.addAction(new FilePropertiesAction());
		controller.addAction(new OpenSourceForgeURLAction("ReportBugAction",  resourceController.getProperty("bugTrackerLocation")));
		controller.addAction(new OpenSourceForgeURLAction("RequestFeatureAction",  resourceController.getProperty("featureTrackerLocation")));
		controller.addAction(new OpenSourceForgeURLAction("AskForHelp",  resourceController.getProperty("helpForumLocation")));
		controller.addAction(new OpenURLAction("FreeplaneHandbook",  resourceController.getProperty("handbook_url")));
		final String defaultMap = resourceController.getProperty("docu_map");
		controller.addAction(new DocumentationAction("DocumentationAction", defaultMap));
		final String tutorialMap = resourceController.getProperty("tutorial_map");
		controller.addAction(new DocumentationAction("TutorialAction", tutorialMap));
		controller.addAction(new HotKeyInfoAction());
	}

}
