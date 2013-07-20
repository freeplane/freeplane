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

import java.awt.Component;
import java.awt.event.ActionEvent;


import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

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
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(map);
		final MapModel styleMap = mapStyleModel.getStyleMap();
		if(styleMap == null){
			UITools.errorMessage(TextUtils.getText("no_styles_found_in_map"));
			return;
		}
		final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
		undoHandler.startTransaction();
		init();
		SModeController modeController = getModeController();
		modeController.getMapController().newMapView(styleMap);
		Controller controller = modeController.getController();
		Component mapViewComponent = controller.getMapViewManager().getMapViewComponent();
		((DialogController) controller.getViewController()).setMapView(mapViewComponent);
		dialog.setLocationRelativeTo(Controller.getCurrentController().getViewController().getJFrame());
		dialog.setVisible(true);
	}

	void commit() {
	    final MapModel currentMap = Controller.getCurrentController().getMap();
	    LogicalStyleController.getController().refreshMap(currentMap);
	    final ModeController currentModeController = Controller.getCurrentModeController();
		currentModeController.commit();
    }

	void rollback() {
	    Controller.getCurrentModeController().rollback();
    }
}
