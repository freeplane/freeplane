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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.icon.IconController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.ShowSelectionAsRectangleAction;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.features.common.addins.mapstyle.MapStyle;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.attribute.ModelessAttributeController;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.attribute.MAttributeController;
import org.freeplane.features.mindmapmode.clipboard.MClipboardController;
import org.freeplane.features.mindmapmode.cloud.MCloudController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;
import org.freeplane.features.mindmapmode.icon.MIconController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.map.ViewLayoutTypeAction;
import org.freeplane.view.swing.map.MapView.Layout;
import org.freeplane.view.swing.map.attribute.EditAttributesAction;
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

	private Controller controller;
	private MModeController modeController;

	MModeController createModeController(JDialog dialog) {
		Controller controller = new Controller();
		final MapViewController mapViewController = new MapViewController();
		final DialogController viewController = new DialogController(controller, mapViewController, dialog);
		controller.setViewController(viewController);
		FilterController.install(controller);
		TextController.install(controller);
		controller.addAction(new ViewLayoutTypeAction(controller, Layout.OUTLINE));
		controller.addAction(new ShowSelectionAsRectangleAction(controller));
		this.controller = controller;
		modeController = new MModeController(controller);
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
        userInputListenerFactory.setMapMouseListener(new DefaultMapMouseListener(controller, new MMouseMotionListener(
            modeController)));
        AttributeController.install(modeController, new MAttributeController(modeController));
        modeController.addAction(new EditAttributesAction(controller));
        final JPopupMenu popupmenu = new JPopupMenu();
        userInputListenerFactory.setNodePopupMenu(popupmenu);
        final FreeplaneToolBar toolbar = new FreeplaneToolBar();
        userInputListenerFactory.addMainToolBar("/main_toolbar", toolbar);
        userInputListenerFactory.setLeftToolBar(((MIconController) IconController.getController(modeController))
            .getIconToolBarScrollPane());
        userInputListenerFactory.setMenuStructure("/xml/stylemodemenu.xml");
        userInputListenerFactory.updateMenus(modeController);
        final MenuBuilder builder = modeController.getUserInputListenerFactory().getMenuBuilder();
        ((MIconController) IconController.getController(modeController)).updateIconToolbar();
        ((MIconController) IconController.getController(modeController)).updateMenus(builder);
        modeController.updateMenus();
        new MapStyle(modeController);
		controller.addModeController(modeController);
		MModeController modeController = this.modeController;
		this.modeController = null;
		return modeController;
	}

	public static void createModeController(ModeController modeController) {
		modeController.addAction(new EditStylesAction(modeController.getController()));
    }
}
