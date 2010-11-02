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
package org.freeplane.features.mindmapmode.styles;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.extension.IExtensionCopier;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.cloud.CloudModel;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.INodeChangeListener;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeChangeEvent;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.LogicalStyleKeys;
import org.freeplane.features.common.styles.LogicalStyleModel;
import org.freeplane.features.common.styles.MapStyle;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.common.styles.ConditionalStyleModel.Item;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class MLogicalStyleController extends LogicalStyleController {
	private final class RemoveConditionalStyleActor implements IActor {
		private final int index;
		private final MapModel map;
		Item item = null;

		private RemoveConditionalStyleActor(MapModel map, int index) {
			this.index = index;
			this.map = map;
		}

		public void undo() {
			MLogicalStyleController.super.insertConditionalStyle(map, index, item.isActive(), item.getCondition(), 
				item.getStyle(), item.isLast());
		}

		public String getDescription() {
			return "RemoveConditionalStyle";
		}

		public void act() {
			item = MLogicalStyleController.super.removeConditionalStyle(map, index);
		}
	}

	private final class AddConditionalStyleActor implements IActor {
		private final MapModel map;
		private final boolean isActive;
		private final ASelectableCondition condition;
		private final IStyle style;
		private boolean isLast;

		public AddConditionalStyleActor(MapModel map, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
			this.map = map;
			this.isActive = isActive;
			this.condition = condition;
			this.style = style;
			this.isLast = isLast;
		}

		public void undo() {
			int index = MapStyleModel.getExtension(map).getConditionalStyleModel().getStyleCount() - 1;
			MLogicalStyleController.super.removeConditionalStyle(map, index);
		}

		public String getDescription() {
			return "AddConditionalStyle";
		}

		public void act() {
			MLogicalStyleController.super.addConditionalStyle(map, isActive, condition, style, isLast);
		}
	}

	private static class StyleRemover implements INodeChangeListener {
		public StyleRemover() {
		}

		public void nodeChanged(final NodeChangeEvent event) {
			final ModeController modeController = Controller.getCurrentModeController();
			if (modeController == null || modeController.isUndoAction()) {
				return;
			}
			if (!event.getProperty().equals(LogicalStyleModel.class)) {
				return;
			}
			final NodeModel node = event.getNode();
			final MapModel map = node.getMap();
			final IStyle styleKey = (IStyle) event.getNewValue();
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

// 	private final ModeController modeController;
	final private List<AssignStyleAction> actions;
	private FilterComposerDialog filterComposerDialog;

	public MLogicalStyleController() {
		super();
//		this.modeController = modeController;
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.getMapController().addNodeChangeListener(new StyleRemover());
		modeController.registerExtensionCopier(new ExtensionCopier());
		modeController.addAction(new RedefineStyleAction());
		modeController.addAction(new ManageConditionalStylesAction());
		actions = new LinkedList<AssignStyleAction>();
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
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
		addStyleMenu(menuBuilder, formatMenuString + "/styles/assign", rootNode, extension);
	}

	private void addStyleMenu(final MenuBuilder menuBuilder, final String category, final NodeModel rootNode, MapStyleModel extension) {
		final List<NodeModel> children = rootNode.getChildren();
		for (final NodeModel child : children) {
			final IStyle style = (IStyle) child.getUserObject();
			if (child.hasChildren()) {
				final String newCategory = category + '/' + style;
				menuBuilder.addMenuItem(category, new JMenu(style.toString()), newCategory, MenuBuilder.AS_CHILD);
				addStyleMenu(menuBuilder, newCategory, child, extension);
			}
			else if(null != extension.getStyleNode(style)){
				final AssignStyleAction action = new AssignStyleAction(style, style.toString(), null);
				actions.add(action);
				menuBuilder.addAction(category, action, MenuBuilder.AS_CHILD);
			}
		}
	}

	public void setStyle(final NodeModel node, final IStyle style) {
		final IStyle oldStyle = LogicalStyleModel.getStyle(node);
		final ModeController modeController = Controller.getCurrentModeController();
		if (oldStyle != null && oldStyle.equals(style) || oldStyle == style) {
			modeController.getMapController().nodeChanged(node, LogicalStyleModel.class, oldStyle, style);
			return;
		}
		final IActor actor = new IActor() {
			public String getDescription() {
				return "setStyle";
			}

			public void act() {
				changeStyle(modeController, node, oldStyle, style);
			}

			public void undo() {
				changeStyle(modeController, node, style, oldStyle);
			}

			private void changeStyle(final ModeController modeController, final NodeModel node, final IStyle oldStyle,
                                    final IStyle style) {
	            if(style != null){
					final LogicalStyleModel model = LogicalStyleModel.createExtension(node);
					model.setStyle(style);
				}
				else{
					node.removeExtension(LogicalStyleModel.class);
				}
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

	public void setStyle(final IStyle style) {
		final ModeController modeController = Controller.getCurrentModeController();
		final List<NodeModel> selectedNodes = modeController.getMapController().getSelectedNodes();
		for (final NodeModel selected : selectedNodes) {
			setStyle(selected, style);
		}
	}

	@Override
	public void moveConditionalStyleDown(final MapModel map, final int index) {
		int maxIndex = MapStyleModel.getExtension(map).getConditionalStyleModel().getStyleCount() - 1;
		if (index < 0 || index >= maxIndex) {
			return;
		}
		IActor actor = new IActor() {
			public String getDescription() {
				return "moveConditionalStyleDown";
			}

			public void act() {
				MLogicalStyleController.super.moveConditionalStyleDown(map, index);
			}

			public void undo() {
				MLogicalStyleController.super.moveConditionalStyleUp(map, index + 1);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
	}

	@Override
	public void moveConditionalStyleUp(final MapModel map, final int index) {
		int maxIndex = MapStyleModel.getExtension(map).getConditionalStyleModel().getStyleCount() - 1;
		if (index <= 0 || index > maxIndex) {
			return;
		}
		IActor actor = new IActor() {
			public String getDescription() {
				return "moveConditionalStyleUp";
			}

			public void act() {
				MLogicalStyleController.super.moveConditionalStyleUp(map, index);
			}

			public void undo() {
				MLogicalStyleController.super.moveConditionalStyleDown(map, index - 1);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
	}

	public static MLogicalStyleController getController() {
		return (MLogicalStyleController) LogicalStyleController.getController();
	}

	@Override
	public void addConditionalStyle(MapModel map, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
		AddConditionalStyleActor actor = new AddConditionalStyleActor(map, isActive, condition, style, isLast);
		Controller.getCurrentModeController().execute(actor, map);
	}

	@Override
	public Item removeConditionalStyle(final MapModel map, final int index) {
		RemoveConditionalStyleActor actor = new RemoveConditionalStyleActor(map, index);
		Controller.getCurrentModeController().execute(actor, map);
		return actor.item;
	}

	public TableModel getConditionalStyleModelAsTableModel(final MapModel map) {
		return new TableModel() {
			private final TableModel tableModel = MapStyleModel.getExtension(map)
				.getConditionalStyleModel().asTableModel();

			public void addTableModelListener(TableModelListener l) {
				tableModel.addTableModelListener(l);
			}

			public Class<?> getColumnClass(int columnIndex) {
				return tableModel.getColumnClass(columnIndex);
			}

			public int getColumnCount() {
				return tableModel.getColumnCount();
			}

			public String getColumnName(int columnIndex) {
				return tableModel.getColumnName(columnIndex);
			}

			public int getRowCount() {
				return tableModel.getRowCount();
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				return tableModel.getValueAt(rowIndex, columnIndex);
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return tableModel.isCellEditable(rowIndex, columnIndex);
			}

			public void removeTableModelListener(TableModelListener l) {
				tableModel.removeTableModelListener(l);
			}

			public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
				final Object oldValue = tableModel.getValueAt(rowIndex, columnIndex);
				if(aValue == oldValue || aValue != null && aValue.equals(oldValue)){
					return;
				}
				IActor actor = new IActor() {

					public String getDescription() {
						return "set conditional style table cell value";
					}

					public void act() {
						tableModel.setValueAt(aValue, rowIndex, columnIndex);
					}

					public void undo() {
						tableModel.setValueAt(oldValue, rowIndex, columnIndex);
					}
				};
				Controller.getCurrentModeController().execute(actor, map);
			}
		};
	}

	public FilterComposerDialog getFilterComposerDialog() {
		if(filterComposerDialog == null){
			filterComposerDialog = new FilterComposerDialog();
		}
		return filterComposerDialog;
    }
	
	public ASelectableCondition editCondition(ASelectableCondition value) {
	    final FilterComposerDialog filterComposerDialog = getFilterComposerDialog();
	    filterComposerDialog.acceptMultipleConditions(false);
	    filterComposerDialog.addCondition(value);
	    filterComposerDialog.show();
	    List<ASelectableCondition> conditions = filterComposerDialog.getConditions();
	    return conditions.isEmpty() ? value : conditions.get(0);
    }
    

}
