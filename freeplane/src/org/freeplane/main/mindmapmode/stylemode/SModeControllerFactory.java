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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.icon.IconController;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
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

	private SModeController modeController;

	SModeController createModeController(JDialog dialog) {
		final Controller controller = new Controller();
		final MapViewController mapViewController = new MapViewController();
		final DialogController viewController = new DialogController(controller, mapViewController, dialog);
		controller.setViewController(viewController);
		FilterController.install(controller);
		TextController.install(controller);
		controller.addAction(new ViewLayoutTypeAction(controller, Layout.OUTLINE));
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
		final SModeController modeController = this.modeController;
		final MapController mapController = modeController.getMapController();
		final StyleEditorPanel styleEditorPanel = new StyleEditorPanel(modeController);
		styleEditorPanel.init();
		JScrollPane styleScrollPane = new JScrollPane(styleEditorPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		dialog.add(styleScrollPane, BorderLayout.EAST);
//		styleEditorPanel.setPreferredSize(new Dimension(200, 200));
		mapController.addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(NodeModel node) {
				final IMapSelection selection = controller.getSelection();
				if(selection.size() == 1 && node.depth() >= 2){
					styleEditorPanel.setStyle(modeController, node);
					return;
				}

				final NodeModel nextSelection;
				if(node.depth() < 2){
					if(node.depth() == 1 && node.hasChildren()){
						nextSelection = (NodeModel) node.getChildAt(0);
					}
					else{
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
			
			public void onDeselect(NodeModel node) {
			}
		});
		mapController.addNodeChangeListener(new INodeChangeListener() {
			public void nodeChanged(NodeChangeEvent event) {
				final IMapSelection selection = controller.getSelection();
				final NodeModel node = event.getNode();
				if(selection.getSelected().equals(node)){
					styleEditorPanel.setStyle(modeController, node);
				}
			}
		});
		this.modeController = null;
		return modeController;
	}

	public static void createModeController(MModeController modeController) {
		modeController.addAction(new EditStylesAction(modeController));
    }
}
