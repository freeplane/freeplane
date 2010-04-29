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
package org.freeplane.features.mindmapmode.addins.export;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.clipboard.ClipboardController;

@ActionLocationDescriptor(accelerator = "control E", locations = { "/menu_bar/file/export/html" })
class ExportToHTMLAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExportToHTMLAction(final Controller controller) {
		super("ExportToHTMLAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = getController().getMap();
		try {
			final File file = ExportAction.chooseFile(getController(), "html", "html", null);
			if (file == null) {
				return;
			}
			ClipboardController.getController(getModeController()).saveHTML(map.getRootNode(), file);
			if (ResourceController.getResourceController().getBooleanProperty("export_icons_in_html")) {
				ExportWithXSLT.copyIconsToDirectory(map, new File(file.getAbsoluteFile().getParentFile(), "icons")
				    .getAbsolutePath());
			}
			((UrlManager) getModeController().getMapController().getModeController().getExtension(UrlManager.class))
			    .loadURL(file.toURI());
		}
		catch (final IOException ex) {
			LogTool.warn(ex);
			UITools.errorMessage(ResourceBundles.getText("export_failed"));
		}
	}
}
