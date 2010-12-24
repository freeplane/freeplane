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
package org.freeplane.features.common.styles;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Compat;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.mindmapmode.file.MFileManager;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
@SuppressWarnings("serial")
class CopyMapStylesAction extends AFreeplaneAction {
	CopyMapStylesAction() {
		super("CopyMapStylesAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final ModeController modeController = controller.getModeController();
		final MFileManager fileManager = MFileManager.getController(modeController);
		final JFileChooser fileChooser = fileManager.getFileChooser(true);
		fileChooser.setMultiSelectionEnabled(false);
		final int returnVal = fileChooser.showOpenDialog(controller.getViewController().getMapView());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fileChooser.getSelectedFile();
		if(! file.exists()){
			return;
		}
		try {
	        final URL url = Compat.fileToUrl(file);
			final MapModel map = controller.getMap();
			MapStyle mapStyleController = MapStyle.getController(modeController);
			mapStyleController.copyStyle(url, map, true);
        }
        catch (MalformedURLException e1) {
	        e1.printStackTrace();
        }
		
	}
}
