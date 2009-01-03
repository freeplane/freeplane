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
package org.freeplane.features.browsemode;

import javax.swing.JPopupMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.MapController;
import org.freeplane.core.ui.components.FreeMindToolBar;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.ui.UserInputListenerFactory;
import org.freeplane.map.attribute.AttributeController;
import org.freeplane.map.clipboard.ClipboardController;
import org.freeplane.map.cloud.CloudController;
import org.freeplane.map.edge.EdgeController;
import org.freeplane.map.icon.IconController;
import org.freeplane.map.link.LinkController;
import org.freeplane.map.nodelocation.LocationController;
import org.freeplane.map.nodestyle.NodeStyleController;
import org.freeplane.map.note.NoteController;
import org.freeplane.map.text.TextController;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class BModeControllerFactory {
	private static BModeController modeController;

	static public BModeController createModeController() {
		if (modeController != null) {
			return modeController;
		}
		modeController = new BModeController();
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(
		    modeController);
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		Controller.getController().addModeController(modeController);
		modeController.setMapController(new MapController(modeController));
		UrlManager.install(modeController, new UrlManager(modeController));
		AttributeController.install(modeController, new AttributeController(modeController));
		LinkController.install(modeController, new LinkController(modeController));
		IconController.install(modeController, new IconController(modeController));
		NodeStyleController.install(modeController, new NodeStyleController(modeController));
		EdgeController.install(modeController, new EdgeController(modeController));
		CloudController.install(modeController, new CloudController(modeController));
		NoteController.install(modeController, new NoteController(modeController));
		TextController.install(modeController, new TextController(modeController));
		ClipboardController.install(modeController, new ClipboardController(modeController));
		LocationController.install(modeController, new LocationController(modeController));
		modeController.getMapController().addNodeSelectionListener(new BNodeNoteViewer());
		final BToolbarContributor toolbarContributor = new BToolbarContributor(modeController);
		modeController.addMenuContributor(toolbarContributor);
		Controller.getController().getViewController()
		    .addMapTitleChangeListener(toolbarContributor);
		userInputListenerFactory.setNodePopupMenu(new JPopupMenu());
		userInputListenerFactory.setMainToolBar(new FreeMindToolBar());
		modeController.updateMenus("org/freeplane/modes/browsemode/menu.xml");
		return modeController;
	}
}
