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
package org.freeplane.features.common.addins.mapstyle;

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ColorTracker;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.ColorUtils;

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
	 * 
	 */
	private final MapStyle mapStyle;

	/**
	 * @param mapStyle
	 */
	MapBackgroundColorAction(final MapStyle mapStyle) {
		super("MapBackgroundColorAction", mapStyle.getController());
		this.mapStyle = mapStyle;
	}

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = getController();
		final MapStyleModel model = (MapStyleModel) mapStyle.getMapHook();
		final Color oldBackgroundColor;
		if (model != null) {
			oldBackgroundColor = model.getBackgroundColor();
		}
		else {
			final String colorPropertyString = ResourceController.getResourceController().getProperty(
			    MapStyle.RESOURCES_BACKGROUND_COLOR);
			oldBackgroundColor = ColorUtils.stringToColor(colorPropertyString);
		}
		final Color actionColor = ColorTracker.showCommonJColorChooserDialog(controller, controller.getSelection()
		    .getSelected(), ResourceBundles.getText("choose_map_background_color"), oldBackgroundColor);
		mapStyle.setBackgroundColor(model, actionColor);
	}
}
