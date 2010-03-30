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
package org.freeplane.main.mindmapmode.stylemode;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.frame.ToggleToolbarAction;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.ShowSelectionAsRectangleAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.features.common.addins.styles.MapStyle;
import org.freeplane.features.common.addins.styles.MapViewLayout;
import org.freeplane.features.common.attribute.ModelessAttributeController;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.addins.styles.StyleEditorPanel;
import org.freeplane.features.mindmapmode.cloud.MCloudController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.features.mindmapmode.icon.MIconController;
import org.freeplane.features.mindmapmode.map.MMapController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.map.ViewLayoutTypeAction;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;
import org.freeplane.view.swing.ui.DefaultNodeMouseMotionListener;
import org.freeplane.view.swing.ui.UserInputListenerFactory;
import org.freeplane.view.swing.ui.mindmapmode.MMouseMotionListener;

/**
 * @author Dimitry Polivaev 24.11.2008
 */
public class SModeControllerFactory {
	private static SModeControllerFactory instance;

	static SModeControllerFactory getInstance() {
		if (instance == null) {
			instance = new SModeControllerFactory();
		}
		return instance;
	}

	private SModeController modeController;

	SModeController createModeController(final JDialog dialog) {
		final Controller controller = new Controller();
		final MapViewController mapViewController = new MapViewController();
		final DialogController viewController = new DialogController(controller, mapViewController, dialog);
		controller.setViewController(viewController);
		FilterController.install(controller);
		TextController.install(controller);
		controller.addAction(new ViewLayoutTypeAction(controller, MapViewLayout.OUTLINE));
		controller.addAction(new ShowSelectionAsRectangleAction(controller));
		modeController = new SModeController(controller);
		modeController.addAction(new NewUserStyleAction(controller));
		modeController.addAction(new DeleteUserStyleAction(controller));
		modeController.addAction(new NewLevelStyleAction(controller));
		modeController.addAction(new DeleteLevelStyleAction(controller));
		final UserInputListenerFactory userInputListenerFactory = new UserInputListenerFactory(modeController);
		userInputListenerFactory.setNodeMouseMotionListener(new DefaultNodeMouseMotionListener(modeController));
		modeController.setUserInputListenerFactory(userInputListenerFactory);
		controller.addExtension(ModelessAttributeController.class, new ModelessAttributeController(controller));
		modeController.setMapController(new MMapController(modeController));
		TextController.install(modeController, new TextController(modeController));
		IconController.install(modeController, new MIconController(modeController));
		NodeStyleController.install(modeController, new MNodeStyleController(modeController));
		EdgeController.install(modeController, new MEdgeController(modeController));
		CloudController.install(modeController, new MCloudController(modeController));
		LinkController.install(modeController, new LinkController(modeController));
		MFileManager.install(modeController, new MFileManager(modeController));
		userInputListenerFactory.setMapMouseListener(new DefaultMapMouseListener(controller, new MMouseMotionListener(
		    modeController)));
		final JPopupMenu popupmenu = new JPopupMenu();
		userInputListenerFactory.setNodePopupMenu(popupmenu);
		final FreeplaneToolBar toolBar = new FreeplaneToolBar("main_toolbar", SwingConstants.HORIZONTAL);
		toolBar.putClientProperty(ViewController.VISIBLE_PROPERTY_KEY, "toolbarVisible");
		userInputListenerFactory.addToolBar("/main_toolbar", ViewController.TOP, toolBar);
		userInputListenerFactory.addToolBar("/icon_toolbar", ViewController.LEFT, ((MIconController) IconController
		    .getController(modeController)).getIconToolBarScrollPane());
		userInputListenerFactory.addToolBar("/status", ViewController.BOTTOM, controller.getViewController()
		    .getStatusBar());
		modeController.addAction(new ToggleToolbarAction(controller, "ToggleLeftToolbarAction", "/icon_toolbar"));
		userInputListenerFactory.setMenuStructure("/xml/stylemodemenu.xml");
		final MenuBuilder builder = modeController.getUserInputListenerFactory().getMenuBuilder();
		userInputListenerFactory.updateMenus(modeController);
		((MIconController) IconController.getController(modeController)).updateIconToolbar();
		((MIconController) IconController.getController(modeController)).updateMenus(builder);
		modeController.updateMenus();
		new MapStyle(modeController, false);
		controller.addModeController(modeController);
		final SModeController modeController = this.modeController;
		final StyleEditorPanel styleEditorPanel = new StyleEditorPanel(modeController, null, false);
		styleEditorPanel.init(modeController);
		final MapController mapController = modeController.getMapController();
		mapController.addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(final NodeModel node) {
				final IMapSelection selection = controller.getSelection();
				if (selection.size() == 1 && node.depth() >= 2) {
					return;
				}
				final NodeModel nextSelection;
				if (node.depth() < 2) {
					if (node.depth() == 1 && node.hasChildren()) {
						nextSelection = (NodeModel) node.getChildAt(0);
					}
					else {
						nextSelection = (NodeModel) (node.getMap().getRootNode().getChildAt(0).getChildAt(0));
					}
				}
				else {
					nextSelection = node;
				}
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						selection.selectAsTheOnlyOneSelected(nextSelection);
					}
				});
			}

			public void onDeselect(final NodeModel node) {
			}
		});
		final JScrollPane styleScrollPane = new JScrollPane(styleEditorPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//		styleEditorPanel.setPreferredSize(new Dimension(200, 200));
		this.modeController = null;
		modeController.getUserInputListenerFactory().addToolBar("/format", ViewController.RIGHT, styleScrollPane);
		return modeController;
	}

	public static void createModeController(final MModeController modeController) {
		modeController.addAction(new EditStylesAction(modeController));
		modeController.addAction(new EditDefaultStylesAction(modeController));
	}
}
