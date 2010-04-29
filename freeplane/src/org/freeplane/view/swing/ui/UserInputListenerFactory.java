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
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.INodeMouseMotionListener;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;

public class UserInputListenerFactory implements IUserInputListenerFactory {
	public static final String NODE_POPUP = "/node_popup";
	final private Controller controller;
	private Component leftToolBar;
	private IMouseListener mapMouseListener;
	private MouseWheelListener mapMouseWheelListener;
	final private ActionListener mapsMenuActionListener;
	private JPopupMenu mapsPopupMenu;
	private FreeplaneMenuBar menuBar;
	private final MenuBuilder menuBuilder;
	private URL menuStructure;
	final private HashSet mRegisteredMouseWheelEventHandler = new HashSet();
	private DragGestureListener nodeDragListener;
	private DropTargetListener nodeDropTargetListener;
	private KeyListener nodeKeyListener;
	private IMouseListener nodeMotionListener;
	private INodeMouseMotionListener nodeMouseMotionListener;
	private JPopupMenu nodePopupMenu;
	private final Map<String, JComponent> toolBars;

	public UserInputListenerFactory(final ModeController modeController) {
		controller = modeController.getController();
		mapsMenuActionListener = new MapsMenuActionListener(controller);
		menuBuilder = new MenuBuilder(modeController);
		controller.getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
			public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
				menuBuilder.afterMapChange(newMap);
			}

			public void afterMapClose(final MapModel oldMap) {
			}

			public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
			}
		});
		toolBars = new LinkedHashMap<String, JComponent>();
	}

	public void addMainToolBar(final String name, final JComponent toolBar) {
		toolBars.put(name, toolBar);
	}

	public void addMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.add(handler);
	}

	public boolean extendSelection(final MouseEvent e) {
		final NodeModel newlySelectedNodeView = ((MainView) e.getComponent()).getNodeView().getModel();
		final boolean extend = e.isControlDown();
		final boolean range = e.isShiftDown();
		final boolean branch = e.isAltGraphDown() || e.isAltDown();
		/* windows alt, linux altgraph .... */
		boolean retValue = false;
		if (extend || range || branch || !controller.getSelection().isSelected(newlySelectedNodeView)) {
			if (!range) {
				if (extend) {
					controller.getSelection().toggleSelected(newlySelectedNodeView);
				}
				else {
					controller.getSelection().selectAsTheOnlyOneSelected(newlySelectedNodeView);
				}
				retValue = true;
			}
			else {
				controller.getSelection().selectContinuous(newlySelectedNodeView);
				retValue = true;
			}
			if (branch) {
				controller.getSelection().selectBranch(newlySelectedNodeView, extend);
				retValue = true;
			}
		}
		if (retValue) {
			e.consume();
		}
		return retValue;
	}

	public Component getLeftToolBar() {
		return leftToolBar;
	}

	public IMouseListener getMapMouseListener() {
		if (mapMouseListener == null) {
			mapMouseListener = new DefaultMapMouseListener(controller, new DefaultMapMouseReceiver(controller));
		}
		return mapMouseListener;
	}

	public MouseWheelListener getMapMouseWheelListener() {
		if (mapMouseWheelListener == null) {
			mapMouseWheelListener = new DefaultMouseWheelListener(controller);
		}
		return mapMouseWheelListener;
	}

	public JPopupMenu getMapPopup() {
		return mapsPopupMenu;
	}

	public FreeplaneMenuBar getMenuBar() {
		if (menuBar == null) {
			menuBar = new FreeplaneMenuBar();
		}
		return menuBar;
	}

	public MenuBuilder getMenuBuilder() {
		return menuBuilder;
	}

	public URL getMenuStructure() {
		return menuStructure;
	}

	public Set getMouseWheelEventHandlers() {
		return Collections.unmodifiableSet(mRegisteredMouseWheelEventHandler);
	}

	public DragGestureListener getNodeDragListener() {
		if (nodeDragListener == null) {
			nodeDragListener = new DefaultNodeDragListener(controller);
		}
		return nodeDragListener;
	}

	public DropTargetListener getNodeDropTargetListener() {
		if (nodeDropTargetListener == null) {
			nodeDropTargetListener = new DefaultNodeDropListener();
		}
		return nodeDropTargetListener;
	}

	public KeyListener getNodeKeyListener() {
		if (nodeKeyListener == null) {
			nodeKeyListener = new DefaultNodeKeyListener(controller, null);
		}
		return nodeKeyListener;
	}

	public IMouseListener getNodeMotionListener() {
		if (nodeMotionListener == null) {
			nodeMotionListener = new DefaultNodeMotionListener();
		}
		return nodeMotionListener;
	}

	public INodeMouseMotionListener getNodeMouseMotionListener() {
		if (nodeMouseMotionListener == null) {
			nodeMouseMotionListener = new DefaultNodeMouseMotionListener(controller.getModeController());
		}
		return nodeMouseMotionListener;
	}

	public JPopupMenu getNodePopupMenu() {
		return nodePopupMenu;
	}

	public JComponent getToolBar(final String name) {
		return toolBars.get(name);
	}

	public Iterable<JComponent> getToolBars() {
		return toolBars.values();
	}

	public void removeMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.remove(handler);
	}

	public void setLeftToolBar(final Component leftToolBar) {
		if (this.leftToolBar != null) {
			throw new RuntimeException("already set");
		}
		this.leftToolBar = leftToolBar;
	}

	public void setMapMouseListener(final IMouseListener mapMouseMotionListener) {
		if (mapMouseListener != null) {
			throw new RuntimeException("already set");
		}
		mapMouseListener = mapMouseMotionListener;
	}

	public void setMapMouseWheelListener(final MouseWheelListener mouseWheelListener) {
		if (mapMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		mapMouseWheelListener = mouseWheelListener;
	}

	public void setMenuBar(final FreeplaneMenuBar menuBar) {
		if (mapMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		this.menuBar = menuBar;
	}

	public void setMenuStructure(final String menuStructureResource) {
		final URL menuStructure = ResourceController.getResourceController().getResource(menuStructureResource);
		setMenuStructure(menuStructure);
	}

	private void setMenuStructure(final URL menuStructure) {
		if (this.menuStructure != null) {
			throw new RuntimeException("already set");
		}
		this.menuStructure = menuStructure;
	}

	public void setNodeDropTargetListener(final DropTargetListener nodeDropTargetListener) {
		if (this.nodeDropTargetListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeDropTargetListener = nodeDropTargetListener;
	}

	public void setNodeKeyListener(final KeyListener nodeKeyListener) {
		if (this.nodeKeyListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeKeyListener = nodeKeyListener;
	}

	public void setNodeMotionListener(final IMouseListener nodeMotionListener) {
		if (this.nodeMotionListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMotionListener = nodeMotionListener;
	}

	public void setNodeMouseMotionListener(final INodeMouseMotionListener nodeMouseMotionListener) {
		if (this.nodeMouseMotionListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMouseMotionListener = nodeMouseMotionListener;
	}

	public void setNodePopupMenu(final JPopupMenu nodePopupMenu) {
		if (this.nodePopupMenu != null) {
			throw new RuntimeException("already set");
		}
		this.nodePopupMenu = nodePopupMenu;
	}

	public void updateMapList() {
		updateModeMenu();
		updateMapList(FreeplaneMenuBar.MAP_POPUP_MENU + "/maps");
		updateMapList(FreeplaneMenuBar.MINDMAP_MENU + "/mindmaps");
	}

	private void updateMapList(final String mapsMenuPosition) {
		menuBuilder.removeChildElements(mapsMenuPosition);
		final IMapViewManager mapViewManager = controller.getMapViewManager();
		final List<MapView> mapViewVector = mapViewManager.getMapViewVector();
		if (mapViewVector == null) {
			return;
		}
		final ButtonGroup group = new ButtonGroup();
		int i = 0;
		for (final MapView mapView : mapViewVector) {
			final String displayName = mapView.getName();
			final JRadioButtonMenuItem newItem = new JRadioButtonMenuItem(displayName);
			newItem.setSelected(false);
			group.add(newItem);
			newItem.addActionListener(mapsMenuActionListener);
			if (displayName.length() > 0) {
				newItem.setMnemonic(displayName.charAt(0));
			}
			final MapView currentMapView = (MapView) mapViewManager.getMapViewComponent();
			if (currentMapView != null) {
				if (mapView == currentMapView) {
					newItem.setSelected(true);
				}
			}
			menuBuilder.addMenuItem(mapsMenuPosition, newItem, mapsMenuPosition + '-' + i++, UIBuilder.AS_CHILD);
		}
	}

	public void updateMenus(final ModeController modeController) {
		final FreeplaneMenuBar menuBar = getMenuBar();
		menuBuilder.addMenuBar(menuBar, FreeplaneMenuBar.MENU_BAR_PREFIX);
		mapsPopupMenu = new JPopupMenu();
		menuBuilder.addPopupMenu(mapsPopupMenu, FreeplaneMenuBar.MAP_POPUP_MENU);
		menuBuilder.addPopupMenu(getNodePopupMenu(), UserInputListenerFactory.NODE_POPUP);
		menuBuilder.addToolbar((JToolBar) getToolBars().iterator().next(), "/main_toolbar");
		mapsPopupMenu.setName(ResourceBundles.getText("mindmaps"));
		if (menuStructure != null) {
			menuBuilder.processMenuCategory(menuStructure);
		}
		final ViewController viewController = controller.getViewController();
		viewController.updateMenus(menuBuilder);
	}

	private void updateModeMenu() {
		menuBuilder.removeChildElements(FreeplaneMenuBar.MODES_MENU);
		final List keys = new LinkedList(controller.getModes());
		for (final ListIterator i = keys.listIterator(); i.hasNext();) {
			final String key = (String) i.next();
			final AFreeplaneAction modesMenuActionListener = new ModesMenuActionListener(key, controller);
			final ModeController modeController = controller.getModeController();
			final boolean isSelected;
			if (modeController != null) {
				isSelected = modeController.getModeName().equals(key);
			}
			else {
				isSelected = false;
			}
			menuBuilder.addRadioItem(FreeplaneMenuBar.MODES_MENU, modesMenuActionListener, isSelected);
			ResourceController.getResourceController().getAdjustableProperty("keystroke_mode_" + key);
		}
	}
}
