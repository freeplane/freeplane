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
package org.freeplane.main.filemode;

import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IPropertyHandler;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodelocation.LocationController;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.MapStyle;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.features.filemode.CenterAction;
import org.freeplane.features.filemode.FMapController;
import org.freeplane.features.filemode.FModeController;
import org.freeplane.features.filemode.OpenPathAction;
import org.freeplane.view.swing.addins.nodehistory.NodeHistory;
import org.freeplane.view.swing.ui.UserInputListenerFactory;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class FModeControllerFactory {
	static private FModeController modeController;

	static public FModeController createModeController(final Controller controller) {
		modeController = new FModeController(controller);
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(modeController);
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		controller.addModeController(modeController);
		modeController.setMapController(new FMapController(modeController));
		UrlManager.install(modeController, new UrlManager(modeController));
		IconController.install(modeController, new IconController(modeController));
		NodeStyleController.install(modeController, new NodeStyleController(modeController));
		EdgeController.install(modeController, new EdgeController(modeController));
		LinkController.install(modeController, new LinkController(modeController));
		TextController.install(modeController, new TextController(modeController));
		CloudController.install(modeController, new CloudController(modeController));
		ClipboardController.install(modeController, new ClipboardController(modeController));
		LocationController.install(modeController, new LocationController(modeController));
		LogicalStyleController.install(modeController, new LogicalStyleController(modeController));
		new MapStyle(modeController, true);
		NodeStyleController.getController(modeController).addShapeGetter(new Integer(0),
		    new IPropertyHandler<String, NodeModel>() {
			    public String getProperty(final NodeModel node, final String currentValue) {
				    return "fork";
			    }
		    });
		modeController.addAction(new CenterAction(controller));
		modeController.addAction(new OpenPathAction(controller));
		userInputListenerFactory.setNodePopupMenu(new JPopupMenu());
		final FreeplaneToolBar toolBar = new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL);
		toolBar.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "toolbarVisible");
		userInputListenerFactory.addToolBar("/main_toolbar", ViewController.TOP, toolBar);
		userInputListenerFactory.addToolBar("/filter_toolbar", ViewController.TOP, FilterController.getController(
		    controller).getFilterToolbar());
		userInputListenerFactory.addToolBar("/status", ViewController.BOTTOM, controller.getViewController()
		    .getStatusBar());
		userInputListenerFactory.setMenuStructure("/xml/filemodemenu.xml");
		userInputListenerFactory.updateMenus(modeController);
		modeController.updateMenus();
		NodeHistory.install(modeController);
		return modeController;
	}
}
