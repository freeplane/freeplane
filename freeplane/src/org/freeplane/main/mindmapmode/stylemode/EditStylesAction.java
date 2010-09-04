/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.main.mindmapmode.stylemode;

import java.awt.event.ActionEvent;


import org.freeplane.core.controller.Controller;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 * 13.09.2009
 */
@SuppressWarnings("serial")
public class EditStylesAction extends AEditStylesAction {
	public EditStylesAction() {
		super("EditStylesAction");
	}
	
	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
		undoHandler.startTransaction();
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(map);
		final MapModel styleMap = mapStyleModel.getStyleMap();
		init();
		getModeController().getMapController().newMapView(styleMap);
		dialog.setLocationRelativeTo(Controller.getCurrentController().getViewController().getJFrame());
		dialog.setVisible(true);
	}

	void commit(final MapModel map) {
	    final MapModel currentMap = Controller.getCurrentController().getMap();
	    LogicalStyleController.getController().refreshMap(currentMap);
	    Controller.getCurrentModeController().commit();
	    Controller.getCurrentModeController().getMapController().setSaved(currentMap, false);
	    final MapController mapController = getModeController().getMapController();
	    mapController.setSaved(map, false);
    }

	void rollback() {
	    Controller.getCurrentModeController().rollback();
    }
}
