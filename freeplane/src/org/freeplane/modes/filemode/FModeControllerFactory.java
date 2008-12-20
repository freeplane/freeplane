/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
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
package org.freeplane.modes.filemode;

import javax.swing.JPopupMenu;

import org.freeplane.controller.Controller;
import org.freeplane.map.IPropertyGetter;
import org.freeplane.map.clipboard.ClipboardController;
import org.freeplane.map.edge.EdgeController;
import org.freeplane.map.icon.IconController;
import org.freeplane.map.link.filemode.FLinkController;
import org.freeplane.map.nodelocation.LocationController;
import org.freeplane.map.nodestyle.NodeStyleController;
import org.freeplane.map.text.TextController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.filemode.FMapController;
import org.freeplane.map.url.UrlManager;
import org.freeplane.ui.FreeMindToolBar;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class FModeControllerFactory {
	static private FModeController modeController;

	static public FModeController createModeController() {
		if (modeController != null) {
			return modeController;
		}
		modeController = new FModeController();
		Controller.getController().addModeController(modeController);
		modeController.setMapController(new FMapController(modeController));
		modeController.setUrlManager(new UrlManager(modeController));
		modeController.setIconController(new IconController(modeController));
		modeController.setNodeStyleController(new NodeStyleController(modeController));
		modeController.setEdgeController(new EdgeController(modeController));
		modeController.setLinkController(new FLinkController(modeController));
		modeController.setTextController(new TextController(modeController));
		modeController.setClipboardController(new ClipboardController(modeController));
		modeController.setLocationController(new LocationController(modeController));
		modeController.getNodeStyleController().addShapeGetter(new Integer(0),
		    new IPropertyGetter<String, NodeModel>() {
			    public String getProperty(final NodeModel node) {
				    return "fork";
			    }
		    });
		modeController.addAction("center", new CenterAction());
		modeController.addAction("openPath", new OpenPathAction());
		modeController.getUserInputListenerFactory().setNodePopupMenu(new JPopupMenu());
		modeController.getUserInputListenerFactory().setMainToolBar(new FreeMindToolBar());
		modeController.updateMenus("org/freeplane/modes/filemode/menu.xml");
		return modeController;
	}
}
