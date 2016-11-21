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
package org.freeplane.main.applet;

import java.security.AccessControlException;

import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.encrypt.EncryptionController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.FoldingController;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.text.TextController;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.UIComponentVisibilityDispatcher;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.freeplane.view.swing.ui.UserInputListenerFactory;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class BModeControllerFactory {
	private static BModeController modeController;

	static public BModeController createModeController() {
		final Controller controller = Controller.getCurrentController();
		modeController = new BModeController(controller);
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(modeController);
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		controller.addModeController(modeController);
		controller.selectModeForBuild(modeController);
		new MapController(modeController);
		IconController.install(new IconController(modeController));
		UrlManager.install(new UrlManager());
		MapIO.install(modeController);
		AttributeController.install(new AttributeController(modeController));
		NodeStyleController.install(new NodeStyleController(modeController));
		EdgeController.install(new EdgeController(modeController));
		CloudController.install(new CloudController(modeController));
		NoteController.install(new NoteController());
		TextController.install(new TextController(modeController));
		LinkController.install(new LinkController(modeController));
		LogicalStyleController.install(new LogicalStyleController(modeController));
		try {
			ClipboardController.install(new ClipboardController());
		}
		catch (final AccessControlException e) {
			LogUtils.warn("can not access system clipboard, clipboard controller disabled");
		}
		LocationController.install(new LocationController());
		SummaryNode.install();
		FreeNode.install();
		MapStyle.install(true);
		final BToolbarContributor toolbarContributor = new BToolbarContributor();
		modeController.addUiBuilder(Phase.ACTIONS, "main_toolbar_url", toolbarContributor);
		controller.getMapViewManager().addMapViewChangeListener(toolbarContributor);
		userInputListenerFactory.setNodePopupMenu(new JPopupMenu());
		final FreeplaneToolBar toolBar = new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL);
		FrameController frameController = (FrameController) controller.getViewController();
		UIComponentVisibilityDispatcher.install(frameController, toolBar, "toolbarVisible");
		userInputListenerFactory.addToolBar("/main_toolbar", ViewController.TOP, toolBar);
		userInputListenerFactory.addToolBar("/filter_toolbar", FilterController.TOOLBAR_SIDE, FilterController.getController(
		    controller).getFilterToolbar());
		userInputListenerFactory.addToolBar("/status", ViewController.BOTTOM, controller.getViewController()
		    .getStatusBar());
		FoldingController.install(new FoldingController());
		new ViewerController();
		EncryptionController.install(new EncryptionController(modeController));
		new AutomaticLayoutController();
		return modeController;
	}
}
