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

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Compat;

class AboutAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	AboutAction(final Controller controller) {
		super("AboutAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final StringBuilder sb = new StringBuilder(ResourceBundles.getText("about_text"));
		sb.append(FreeplaneVersion.getVersion());
		sb.append('\n');
		sb.append(FpStringUtils.format("java_version", Compat.JAVA_VERSION));
		sb.append('\n');
		sb.append(FpStringUtils.format("main_resource_directory", ResourceController.getResourceController()
		    .getResourceBaseDir()));
		sb.append('\n');
		sb.append(FpStringUtils.format("user_config_folder", ResourceController.getResourceController()
		    .getFreeplaneUserDirectory()));
		JOptionPane.showMessageDialog(getController().getViewController().getViewport(), sb.toString(), ResourceBundles
		    .getText("AboutAction.text"), JOptionPane.INFORMATION_MESSAGE);
	}
}
