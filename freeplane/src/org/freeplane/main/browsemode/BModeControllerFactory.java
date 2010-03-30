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
package org.freeplane.main.browsemode;

import java.security.AccessControlException;

import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.browsemode.BModeController;
import org.freeplane.features.browsemode.BNodeNoteViewer;
import org.freeplane.features.browsemode.BToolbarContributor;
import org.freeplane.features.common.addins.styles.MapStyle;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.nodelocation.LocationController;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.features.mindmapmode.addins.UnfoldAll;
import org.freeplane.view.swing.addins.filepreview.ViewerController;
import org.freeplane.view.swing.addins.nodehistory.NodeHistory;
import org.freeplane.view.swing.ui.UserInputListenerFactory;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class BModeControllerFactory {
	private static BModeController modeController;

	static public BModeController createModeController(final Controller controller, final String menuStructure) {
		modeController = new BModeController(controller);
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(modeController);
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		controller.addModeController(modeController);
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
		try {
			ClipboardController.install(modeController, new ClipboardController(modeController));
		}
		catch (final AccessControlException e) {
			LogTool.warn("can not access system clipboard, clipboard controller disabled");
		}
		LocationController.install(modeController, new LocationController(modeController));
		new MapStyle(modeController, true);
		modeController.getMapController().addNodeSelectionListener(new BNodeNoteViewer(modeController.getController()));
		final BToolbarContributor toolbarContributor = new BToolbarContributor(modeController);
		modeController.addMenuContributor(toolbarContributor);
		controller.getMapViewManager().addMapViewChangeListener(toolbarContributor);
		userInputListenerFactory.setNodePopupMenu(new JPopupMenu());
		final FreeplaneToolBar toolBar = new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL);
		toolBar.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "toolbarVisible");
		userInputListenerFactory.addToolBar("/main_toolbar", ViewController.TOP, toolBar);
		userInputListenerFactory.addToolBar("/filter_toolbar", ViewController.TOP, FilterController.getController(
		    controller).getFilterToolbar());
		userInputListenerFactory.addToolBar("/status", ViewController.BOTTOM, controller.getViewController()
		    .getStatusBar());
		userInputListenerFactory.setMenuStructure(menuStructure);
		final UnfoldAll unfoldAll = new UnfoldAll(modeController);
		for (final AFreeplaneAction annotatedAction : unfoldAll.getAnnotatedActions()) {
			modeController.addAction(annotatedAction);
		}
		userInputListenerFactory.updateMenus(modeController);
		modeController.updateMenus();
		new ViewerController(modeController);
		NodeHistory.install(modeController);
		return modeController;
	}
}
