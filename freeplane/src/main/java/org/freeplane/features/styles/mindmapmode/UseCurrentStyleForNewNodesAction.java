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
package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyle;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class UseCurrentStyleForNewNodesAction extends AFreeplaneAction {

	public UseCurrentStyleForNewNodesAction() {
		super("UseCurrentStyleForNewNodesAction");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void actionPerformed(final ActionEvent e) {
        final Controller controller = Controller.getCurrentController();
        final NodeModel node = controller.getSelection().getSelected();
        final ModeController modeController = controller.getModeController();
        final MapStyle mapStyleController = MapStyle.getController(modeController);
        IStyle style = LogicalStyleModel.getStyle(node);
        final MapModel map = node.getMap();
        String propertyValue = NewNodeStyle.propertyValue(style);
        mapStyleController.setProperty(map, NewNodeStyle.NEW_NODE_STYLE_PROPERTY_NAME, propertyValue);
	}

}
