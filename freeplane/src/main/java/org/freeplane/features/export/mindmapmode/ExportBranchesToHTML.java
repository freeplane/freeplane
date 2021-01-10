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

import org.freeplane.core.ui.CaseSensitiveFileNameExtensionFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.List;

class ExportBranchesToHTML implements IExportEngine {

	public ExportBranchesToHTML() {
	}

	public void export(List<NodeModel> branches, File file) {
		try {
			MapClipboardController.getController().saveHTML(branches, file);
			((UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class))
			    .loadURL(file.toURI());
		}
		catch (final IOException ex) {
			LogUtils.warn(ex);
			UITools.errorMessage(TextUtils.getText("export_failed"));
		}
	}

	public FileFilter getFileFilter() {
	    return new CaseSensitiveFileNameExtensionFilter("html", TextUtils.getText("ExportBranchToHTMLAction.text"));
    }
}
