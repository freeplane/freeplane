/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.view.swing.features.filepreview.MindMapPreviewWithOptions;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
@SuppressWarnings("serial")
class ReplaceMapStylesAction extends AFreeplaneAction {
	ReplaceMapStylesAction() {
		super("ReplaceMapStylesAction");
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final Controller controller = Controller.getCurrentController();
		final ModeController modeController = controller.getModeController();
		final MFileManager fileManager = MFileManager.getController(modeController);
		final JFileChooser fileChooser = fileManager.getMindMapFileChooser();
		MindMapPreviewWithOptions previewOptions = new MindMapPreviewWithOptions(fileChooser, false);
		fileChooser.setAccessory(previewOptions);
		fileChooser.setMultiSelectionEnabled(false);
		final int returnVal = fileChooser.showOpenDialog(controller.getMapViewManager().getMapViewComponent());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fileChooser.getSelectedFile();
		if(! file.exists()){
			return;
		}
		try {
			final URL url = file.toURI().toURL();
			final MapModel map = controller.getMap();
			MapStyle mapStyleController = MapStyle.getController(modeController);
			mapStyleController.replaceStyle(url, map);
			mapStyleController.setProperty(map, MapStyleModel.FOLLOWED_MAP_LOCATION_PROPERTY, previewOptions.isFollowChecked() ? url.toURI().toString() : null);
        }
        catch (MalformedURLException | URISyntaxException e) {
	        LogUtils.severe(e);
        }

	}
}
