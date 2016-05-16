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
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;


class DocumentationAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private final String document;

	DocumentationAction( final String actionName, final String document) {
		super(actionName);
		this.document = document;
	}

	public void actionPerformed(final ActionEvent e) {
		final ResourceController resourceController = ResourceController.getResourceController();
		final File userDir = new File(resourceController.getFreeplaneUserDirectory());
		final File baseDir = new File(resourceController.getInstallationBaseDir());
		final String languageCode = resourceController.getLanguageCode();
		final File file = ConfigurationUtils.getLocalizedFile(new File[]{userDir, baseDir}, document, languageCode);
		if(file == null){
			String name = (String) getValue(Action.NAME);
			String errorMessage = TextUtils.format("invalid_file_msg", name);
			UITools.errorMessage(errorMessage);
			return;
		}
		try {
			final URL endUrl = file.toURL();
			UITools.executeWhenNodeHasFocus(new Runnable() {
				public void run() {
					try {
						if (endUrl.getFile().endsWith(".mm")) {
							 Controller.getCurrentController().selectMode(MModeController.MODENAME);
							 ((MMapController)Controller.getCurrentModeController().getMapController()).newDocumentationMap(endUrl);
						}
						else {
							Controller.getCurrentController().getViewController().openDocument(endUrl);
						}
					}
					catch (final Exception e1) {
						LogUtils.severe(e1);
					}
				}
			});
		}
		catch (final MalformedURLException e1) {
			LogUtils.warn(e1);
		}
	}
	
	@Override
	public void afterMapChange(final Object newMap) {
	}
	
}
