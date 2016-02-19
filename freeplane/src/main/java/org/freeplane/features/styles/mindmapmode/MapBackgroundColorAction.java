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

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ColorTracker;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 * Mar 12, 2009
 */
class MapBackgroundColorAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param mapStyle
	 */
	MapBackgroundColorAction() {
		super("MapBackgroundColorAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		MapStyle mapStyle = (MapStyle) controller.getModeController().getExtension(MapStyle.class);
		final MapStyleModel model = (MapStyleModel) mapStyle.getMapHook(controller.getMap());
		final Color oldBackgroundColor;
		final String colorPropertyString = ResourceController.getResourceController().getProperty(
		    MapStyle.RESOURCES_BACKGROUND_COLOR);
		final Color defaultBgColor = ColorUtils.stringToColor(colorPropertyString);
		if (model != null) {
			oldBackgroundColor = model.getBackgroundColor();
		}
		else {
			oldBackgroundColor = defaultBgColor;
		}
		final Color actionColor = ColorTracker.showCommonJColorChooserDialog(controller.getSelection()
		    .getSelected(), TextUtils.getText("choose_map_background_color"), oldBackgroundColor, defaultBgColor);
		mapStyle.setBackgroundColor(model, actionColor);
	}
}
