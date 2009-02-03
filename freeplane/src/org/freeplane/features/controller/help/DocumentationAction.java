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
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.browsemode.BModeController;

class DocumentationAction extends AbstractAction {
	final private Controller controller;

	DocumentationAction(final Controller controller) {
		this.controller = controller;
		MenuBuilder.setLabelAndMnemonic(this, ResourceController.getText("documentation"));
	}

	public void actionPerformed(final ActionEvent e) {
		String map = ResourceController.getText("browsemode_initial_map");
		map = ResourceController.removeTranslateComment(map);
		if (map != null && map != "") {
			URL url = null;
			url = ResourceController.getResourceController().getResource(map);
			final URL endUrl = url;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						if (controller.selectMode(BModeController.MODENAME)) {
							((BModeController) controller.getModeController()).getMapController().newMap(endUrl);
						}
					}
					catch (final Exception e1) {
						org.freeplane.core.util.Tools.logException(e1);
					}
				}
			});
		}
	}
}
