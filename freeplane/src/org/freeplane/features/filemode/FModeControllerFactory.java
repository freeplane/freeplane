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
package org.freeplane.features.filemode;

import javax.swing.JPopupMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.IPropertyGetter;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.components.FreeMindToolBar;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.nodelocation.LocationController;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.ui.UserInputListenerFactory;

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
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(
		    modeController);
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		Controller.getController().addModeController(modeController);
		modeController.setMapController(new FMapController(modeController));
		UrlManager.install(modeController, new UrlManager(modeController));
		IconController.install(modeController, new IconController(modeController));
		NodeStyleController.install(modeController, new NodeStyleController(modeController));
		EdgeController.install(modeController, new EdgeController(modeController));
		LinkController.install(modeController, new LinkController(modeController));
		TextController.install(modeController, new TextController(modeController));
		ClipboardController.install(modeController, new ClipboardController(modeController));
		LocationController.install(modeController, new LocationController(modeController));
		NodeStyleController.getController(modeController).addShapeGetter(new Integer(0),
		    new IPropertyGetter<String, NodeModel>() {
			    public String getProperty(final NodeModel node, final String currentValue) {
				    return "fork";
			    }
		    });
		modeController.addAction("center", new CenterAction());
		modeController.addAction("openPath", new OpenPathAction());
		userInputListenerFactory.setNodePopupMenu(new JPopupMenu());
		userInputListenerFactory.setMainToolBar(new FreeMindToolBar());
		modeController.updateMenus("org/freeplane/modes/filemode/menu.xml");
		return modeController;
	}
}
