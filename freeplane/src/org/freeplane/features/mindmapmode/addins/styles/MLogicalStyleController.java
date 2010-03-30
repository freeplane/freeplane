/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.mindmapmode.addins.styles;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;

import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.extension.IExtensionCopier;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.addins.styles.LogicalStyleController;
import org.freeplane.features.common.addins.styles.LogicalStyleKeys;
import org.freeplane.features.common.addins.styles.LogicalStyleModel;
import org.freeplane.features.common.addins.styles.MapStyle;
import org.freeplane.features.common.addins.styles.MapStyleModel;
import org.freeplane.features.common.cloud.CloudModel;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.INodeChangeListener;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeChangeEvent;
import org.freeplane.features.common.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class MLogicalStyleController extends LogicalStyleController {
	private static class StyleRemover implements INodeChangeListener {
		public StyleRemover() {
		}

		public void nodeChanged(final NodeChangeEvent event) {
			final ModeController modeController = event.getModeController();
			if (modeController == null || modeController.isUndoAction()) {
				return;
			}
			if (!event.getProperty().equals(LogicalStyleModel.class)) {
				return;
			}
			final NodeModel node = event.getNode();
			final MapModel map = node.getMap();
			final Object styleKey = event.getNewValue();
			final MapStyleModel mapStyles = MapStyleModel.getExtension(map);
			final NodeModel styleNode = mapStyles.getStyleNode(styleKey);
			if (styleNode == null) {
				return;
			}
			modeController.undoableRemoveExtensions(LogicalStyleKeys.NODE_STYLE, node, styleNode);
		}
	};

	private static class ExtensionCopier implements IExtensionCopier {
		public void copy(final Object key, final NodeModel from, final NodeModel to) {
			if (!key.equals(LogicalStyleKeys.LOGICAL_STYLE)) {
				return;
			}
			copy(from, to);
		}

		public void copy(final NodeModel from, final NodeModel to) {
			final LogicalStyleModel fromStyle = (LogicalStyleModel) from.getExtension(LogicalStyleModel.class);
			if (fromStyle == null) {
				return;
			}
			final LogicalStyleModel toStyle = LogicalStyleModel.createExtension(to);
			toStyle.setStyle(fromStyle.getStyle());
		}

		public void remove(final Object key, final NodeModel from) {
			if (!key.equals(LogicalStyleKeys.LOGICAL_STYLE)) {
				return;
			}
			from.removeExtension(CloudModel.class);
		}

		public void remove(final Object key, final NodeModel from, final NodeModel which) {
			if (!key.equals(LogicalStyleKeys.LOGICAL_STYLE)) {
				return;
			}
			final LogicalStyleModel whichStyle = (LogicalStyleModel) which.getExtension(LogicalStyleModel.class);
			if (whichStyle == null) {
				return;
			}
			final LogicalStyleModel fromStyle = (LogicalStyleModel) from.getExtension(LogicalStyleModel.class);
			if (fromStyle == null) {
				return;
			}
			from.removeExtension(fromStyle);
		}
	}

	private final ModeController modeController;
	final private List<AssignStyleAction> actions;

	public MLogicalStyleController(final ModeController modeController) {
		super(modeController);
		this.modeController = modeController;
		modeController.getMapController().addNodeChangeListener(new StyleRemover());
		modeController.registerExtensionCopier(new ExtensionCopier());
		modeController.addAction(new RedefineStyleAction(modeController.getController()));
		actions = new LinkedList<AssignStyleAction>();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		modeController.getController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
			public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
				removeStyleMenu(menuBuilder, "/menu_bar");
				removeStyleMenu(menuBuilder, "/node_popup");
			}

			public void afterMapClose(final MapModel oldMap) {
			}

			public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
				addStyleMenu(menuBuilder, "/menu_bar", newMap);
				addStyleMenu(menuBuilder, "/node_popup", newMap);
			}
		});
		final MapController mapController = modeController.getMapController();
		mapController.addMapChangeListener(new IMapChangeListener() {
			public void onPreNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
			                           final NodeModel child, final int newIndex) {
			}

			public void onPreNodeDelete(final NodeModel oldParent, final NodeModel selectedNode, final int index) {
			}

			public void onNodeMoved(final NodeModel oldParent, final int oldIndex, final NodeModel newParent,
			                        final NodeModel child, final int newIndex) {
			}

			public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
			}

			public void onNodeDeleted(final NodeModel parent, final NodeModel child, final int index) {
			}

			public void mapChanged(final MapChangeEvent event) {
				if (event.getProperty().equals(MapStyle.MAP_STYLES)) {
					removeStyleMenu(menuBuilder, "/menu_bar");
					addStyleMenu(menuBuilder, "/menu_bar", event.getMap());
					removeStyleMenu(menuBuilder, "/node_popup");
					addStyleMenu(menuBuilder, "/node_popup", event.getMap());
				}
			}
		});
		mapController.addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(final NodeModel node) {
				selectActions();
			}

			public void onDeselect(final NodeModel node) {
			}
		});
	}

	protected void removeStyleMenu(final MenuBuilder menuBuilder, final String formatMenuString) {
		menuBuilder.removeChildElements(formatMenuString + "/styles/assign");
		actions.clear();
	}

	protected void addStyleMenu(final MenuBuilder menuBuilder, final String formatMenuString, final MapModel newMap) {
		if (newMap == null) {
			return;
		}
		final MapStyleModel extension = MapStyleModel.getExtension(newMap);
		if (extension == null) {
			return;
		}
		final NodeModel rootNode = extension.getStyleMap().getRootNode();
		addStyleMenu(menuBuilder, formatMenuString + "/styles/assign", rootNode);
	}

	private void addStyleMenu(final MenuBuilder menuBuilder, final String category, final NodeModel rootNode) {
		final List<NodeModel> children = rootNode.getChildren();
		for (final NodeModel child : children) {
			final Object style = child.getUserObject();
			if (child.hasChildren()) {
				final String newCategory = category + '/' + style;
				menuBuilder.addMenuItem(category, new JMenu(style.toString()), newCategory, MenuBuilder.AS_CHILD);
				addStyleMenu(menuBuilder, newCategory, child);
			}
			else {
				final AssignStyleAction action = new AssignStyleAction(style, modeController.getController(), style
				    .toString(), null);
				actions.add(action);
				menuBuilder.addAction(category, action, MenuBuilder.AS_CHILD);
			}
		}
	}

	public void setStyle(final NodeModel node, final Object style) {
		final LogicalStyleModel model = LogicalStyleModel.createExtension(node);
		final Object oldStyle = model.getStyle();
		if (oldStyle != null && oldStyle.equals(style) || oldStyle == style) {
			modeController.getMapController().nodeChanged(node, LogicalStyleModel.class, oldStyle, style);
			return;
		}
		final IActor actor = new IActor() {
			public void undo() {
				model.setStyle(oldStyle);
				modeController.getMapController().nodeChanged(node, LogicalStyleModel.class, style, oldStyle);
				selectActions();
			}

			public String getDescription() {
				return "setStyle";
			}

			public void act() {
				model.setStyle(style);
				modeController.getMapController().nodeChanged(node, LogicalStyleModel.class, oldStyle, style);
				selectActions();
			}
		};
		modeController.execute(actor, node.getMap());
	}

	void selectActions() {
		for (final AssignStyleAction action : actions) {
			action.setSelected();
		}
	}

	public void setStyle(final Object style) {
		final List<NodeModel> selectedNodes = modeController.getMapController().getSelectedNodes();
		for (final NodeModel selected : selectedNodes) {
			setStyle(selected, style);
		}
	}
}
