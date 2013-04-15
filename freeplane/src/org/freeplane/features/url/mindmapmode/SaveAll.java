/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.url.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.freeplane.core.ui.AFreeplaneAction;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

/**
 * @author foltin
 */

public class SaveAll extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public SaveAll() {
		super("SaveAll");
	}

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final Component initialMapView = controller.getMapViewManager().getMapViewComponent();
		final Map<String, MapModel> mapViews = getMapViews();
		final Iterator<Entry<String, MapModel>> iterator = mapViews.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<String, MapModel> entry = iterator.next();
			controller.getMapViewManager().changeToMapView(entry.getKey());
			final ModeController modeController = controller.getModeController();
			if (modeController instanceof MModeController) {
				((MModeController) modeController).save();
			}
		}
		if (initialMapView != null) {
			controller.getMapViewManager().changeToMapView(initialMapView);
		}
	}

	/**
	 */
	private Map<String, MapModel> getMapViews() {
		return Controller.getCurrentController().getMapViewManager().getMaps();
	}
}
