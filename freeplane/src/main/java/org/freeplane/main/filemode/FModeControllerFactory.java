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

import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.resizer.UIComponentVisibilityDispatcher;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.filemode.CenterAction;
import org.freeplane.features.map.filemode.FMapController;
import org.freeplane.features.map.filemode.OpenPathAction;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.filemode.FModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.text.TextController;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.features.nodehistory.NodeHistory;
import org.freeplane.view.swing.ui.UserInputListenerFactory;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class FModeControllerFactory {
	static private FModeController modeController;

	static public FModeController createModeController() {
		final Controller controller = Controller.getCurrentController();
		modeController = new FModeController(controller);
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(modeController);
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		controller.addModeController(modeController);
		controller.selectModeForBuild(modeController);
		new FMapController(modeController);
		UrlManager.install(new UrlManager());
		MapIO.install(modeController);
		new IconController(modeController).install(modeController);
		NodeStyleController.install(new NodeStyleController(modeController));
		EdgeController.install(new EdgeController(modeController));
		new TextController(modeController).install(modeController);
		LinkController.install(new LinkController(modeController));
		CloudController.install(new CloudController(modeController));
		ClipboardController.install(new ClipboardController());
		LocationController.install(new LocationController());
		LogicalStyleController.install(new LogicalStyleController(modeController));
		MapStyle.install(true);
		NodeStyleController.getController().addShapeGetter(new Integer(0),
		    new IPropertyHandler<ShapeConfigurationModel, NodeModel>() {
			    @Override
				public ShapeConfigurationModel getProperty(final NodeModel node, final ShapeConfigurationModel currentValue) {
				    return ShapeConfigurationModel.FORK;
			    }
		    });
		modeController.addAction(new CenterAction());
		modeController.addAction(new OpenPathAction());
		userInputListenerFactory.setNodePopupMenu(new JPopupMenu());
		final FreeplaneToolBar toolBar = new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL);
		FrameController frameController = (FrameController) controller.getViewController();
		UIComponentVisibilityDispatcher.install(frameController.getPropertyKeyPrefix(), toolBar, "toolbarVisible");
		userInputListenerFactory.addToolBar("/main_toolbar", ViewController.TOP, toolBar);
		userInputListenerFactory.addToolBar("/filter_toolbar", FilterController.TOOLBAR_SIDE, FilterController.getCurrentFilterController().getFilterToolbar());
		userInputListenerFactory.addToolBar("/status", ViewController.BOTTOM, controller.getViewController().getStatusBar());
		NodeHistory.install(modeController);
		return modeController;
	}
}
