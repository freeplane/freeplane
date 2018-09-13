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
package org.freeplane.features.export.mindmapmode;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileFilter;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ExampleFileFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;

class ExportToHTML implements IExportEngine {
	public ExportToHTML() {
		super();
	}

	public FileFilter getFileFilter() {
		return new ExampleFileFilter("html", TextUtils.getText("ExportToHTMLAction.text"));
    }
	public void export(MapModel map, ExportedXmlWriter xmlWriter, File file) {
		try {
			ClipboardController.getController().saveHTML(map.getRootNode(), file);
			if (ResourceController.getResourceController().getBooleanProperty("export_icons_in_html")) {
				ExportWithXSLT.copyIconsToDirectory(map, new File(file.getAbsoluteFile().getParentFile(), "icons")
				    .getAbsolutePath());
			}
			((UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class))
			    .loadURL(file.toURI());
		}
		catch (final IOException ex) {
			LogUtils.warn(ex);
			UITools.errorMessage(TextUtils.getText("export_failed"));
		}
	}

}
