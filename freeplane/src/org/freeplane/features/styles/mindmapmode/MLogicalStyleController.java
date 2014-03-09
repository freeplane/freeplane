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
package org.freeplane.features.styles.mindmapmode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.ConditionalStyleModel.Item;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleKeys;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.view.swing.features.filepreview.MapBackgroundClearAction;
import org.freeplane.view.swing.features.filepreview.MapBackgroundImageAction;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class MLogicalStyleController extends LogicalStyleController {
	private final class RemoveConditionalStyleActor implements IActor {
		private final int index;
		private final ConditionalStyleModel conditionalStyleModel;
		Item item = null;

		private RemoveConditionalStyleActor(ConditionalStyleModel conditionalStyleModel, int index) {
			this.index = index;
			this.conditionalStyleModel = conditionalStyleModel;
		}

		public void undo() {
			MLogicalStyleController.super.insertConditionalStyle(conditionalStyleModel, index, item.isActive(), item.getCondition(), 
				item.getStyle(), item.isLast());
		}

		public String getDescription() {
			return "RemoveConditionalStyle";
		}

		public void act() {
			item = MLogicalStyleController.super.removeConditionalStyle(conditionalStyleModel, index);
		}
	}

	private final class AddConditionalStyleActor implements IActor {
		private final ConditionalStyleModel conditionalStyleModel;
		private final boolean isActive;
		private final ASelectableCondition condition;
		private final IStyle style;
		private boolean isLast;

		public AddConditionalStyleActor(final ConditionalStyleModel conditionalStyleModel, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
			this.conditionalStyleModel = conditionalStyleModel;
			this.isActive = isActive;
			this.condition = condition;
			this.style = style;
			this.isLast = isLast;
		}

		public void undo() {
			int index = conditionalStyleModel.getStyleCount() - 1;
			MLogicalStyleController.super.removeConditionalStyle(conditionalStyleModel, index);
		}

		public String getDescription() {
			return "AddConditionalStyle";
		}

		public void act() {
			MLogicalStyleController.super.addConditionalStyle(conditionalStyleModel, isActive, condition, style, isLast);
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
			from.removeExtension(LogicalStyleModel.class);
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
		
		public void resolveParentExtensions(Object key, NodeModel to) {
        }
	}

// 	private final ModeController modeController;
	final private List<AssignStyleAction> actions;
	private FilterComposerDialog filterComposerDialog;

	public MLogicalStyleController(ModeController modeController) {
		super(modeController);
//		this.modeController = modeController;
		actions = new LinkedList<AssignStyleAction>();
	}

	public void initS() {
	    final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new ManageNodeConditionalStylesAction());
	}
	public void initM() {
	    final ModeController modeController = Controller.getCurrentModeController();
		modeController.getMapController().addNodeChangeListener(new StyleRemover());
		modeController.registerExtensionCopier(new ExtensionCopier());
		modeController.addAction(new RedefineStyleAction());
		modeController.addAction(new NewUserStyleAction());
		modeController.addAction(new ManageMapConditionalStylesAction());
		modeController.addAction(new ManageNodeConditionalStylesAction());
		modeController.addAction(new CopyStyleExtensionsAction());
		if (modeController.getModeName().equals("MindMap")) {
			modeController.addAction(new MapBackgroundColorAction());
			modeController.addAction(new MapBackgroundImageAction());
			modeController.addAction(new MapBackgroundClearAction());
			modeController.addAction(new SetBooleanMapPropertyAction(MapStyle.FIT_TO_VIEWPORT));
			modeController.addAction(new CopyMapStylesAction());
		}
		if(! modeController.getController().getViewController().isHeadless()){
			final IUserInputListenerFactory userInputListenerFactory = modeController.getUserInputListenerFactory();
			final MenuBuilder menuBuilder = userInputListenerFactory.getMenuBuilder(MenuBuilder.class);
			//TODO RIBBONS - apply to ribbons as well
			Controller.getCurrentController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
				public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
					removeStyleMenu(menuBuilder, "main_menu_styles");
					removeStyleMenu(menuBuilder, "node_popup_styles");
				}

				public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
					addStyleMenu(menuBuilder, "main_menu_styles", newMap);
					addStyleMenu(menuBuilder, "node_popup_styles", newMap);
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
						removeStyleMenu(menuBuilder, "main_menu_styles");
						addStyleMenu(menuBuilder, "main_menu_styles", event.getMap());
						removeStyleMenu(menuBuilder, "node_popup_styles");
						addStyleMenu(menuBuilder, "node_popup_styles", event.getMap());
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
	}

	protected void removeStyleMenu(final MenuBuilder menuBuilder, final String formatMenuString) {
		if(null != menuBuilder.get(formatMenuString))
			menuBuilder.removeChildElements(formatMenuString);
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
		 if(null == menuBuilder.get(formatMenuString))
		     return;
		 final NodeModel rootNode = extension.getStyleMap().getRootNode();
			final AssignStyleAction resetAction = new AssignStyleAction(null);
			actions.add(resetAction);
			menuBuilder.addAction(formatMenuString, resetAction, MenuBuilder.AS_CHILD);
		 addStyleMenu(menuBuilder, formatMenuString, rootNode, extension);
	}

	private void addStyleMenu(final MenuBuilder menuBuilder, final String category, final NodeModel rootNode, MapStyleModel extension) {
		final List<NodeModel> children = rootNode.getChildren();
		for (final NodeModel child : children) {
			final IStyle style = (IStyle) child.getUserObject();
			if (child.hasChildren()) {
				addStyleMenu(menuBuilder, category, child, extension);
			}
			else if(null != extension.getStyleNode(style)){
				final AssignStyleAction action = new AssignStyleAction(style);
				actions.add(action);
				menuBuilder.addAction(category, action, MenuBuilder.AS_CHILD);
			}
		}
	}

	public void setStyle(final NodeModel node, final IStyle style) {
		final ModeController modeController = Controller.getCurrentModeController();
        final IStyle oldStyle = LogicalStyleModel.getStyle(node);
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

    public void copyStyleExtensions(final IStyle style, final NodeModel target) {
        final MTextController textController = MTextController.getController();
        final MapStyleModel extension = MapStyleModel.getExtension(target.getMap());
        final NodeModel styleNode = extension.getStyleNode(style);
        if(styleNode != null){
            final MAttributeController attributeController = MAttributeController.getController();
            attributeController.copyAttributesToNode(styleNode, target);
            final String detailTextText = DetailTextModel.getDetailTextText(styleNode);
            if(detailTextText != null)
                textController.setDetails(target, detailTextText);
            final String noteText = NoteModel.getNoteText(styleNode);
            if(noteText != null)
            {
            	MNoteController noteController = (MNoteController) NoteController.getController();
            	noteController.setNoteText(target, noteText);
            }
        }
    }

	void selectActions() {
		for (final AssignStyleAction action : actions) {
			action.setSelected();
		}
	}

	public void setStyle(final IStyle style) {
		final ModeController modeController = Controller.getCurrentModeController();
		final Collection<NodeModel> selectedNodes = modeController.getMapController().getSelectedNodes();
		for (final NodeModel selected : selectedNodes) {
			setStyle(selected, style);
		}
	}

	
	public void moveConditionalStyleDown(final MapModel map, final ConditionalStyleModel conditionalStyleModel, final int index) {
		int maxIndex = conditionalStyleModel.getStyleCount() - 1;
		if (index < 0 || index >= maxIndex) {
			return;
		}
		IActor actor = new IActor() {
			public String getDescription() {
				return "moveConditionalStyleDown";
			}

			public void act() {
				MLogicalStyleController.super.moveConditionalStyleDown(conditionalStyleModel, index);
			}

			public void undo() {
				MLogicalStyleController.super.moveConditionalStyleUp(conditionalStyleModel, index + 1);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
	}

	
	public void moveConditionalStyleUp(final MapModel map, final ConditionalStyleModel conditionalStyleModel, final int index) {
		int maxIndex = conditionalStyleModel.getStyleCount() - 1;
		if (index <= 0 || index > maxIndex) {
			return;
		}
		IActor actor = new IActor() {
			public String getDescription() {
				return "moveConditionalStyleUp";
			}

			public void act() {
				MLogicalStyleController.super.moveConditionalStyleUp(conditionalStyleModel, index);
			}

			public void undo() {
				MLogicalStyleController.super.moveConditionalStyleDown(conditionalStyleModel, index - 1);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
	}

	public static MLogicalStyleController getController() {
		return (MLogicalStyleController) LogicalStyleController.getController();
	}

	
	public void addConditionalStyle(final MapModel map, final ConditionalStyleModel conditionalStyleModel, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
		AddConditionalStyleActor actor = new AddConditionalStyleActor(conditionalStyleModel, isActive, condition, style, isLast);
		Controller.getCurrentModeController().execute(actor, map);
	}

	
	public Item removeConditionalStyle(final MapModel map, final ConditionalStyleModel conditionalStyleModel, final int index) {
		RemoveConditionalStyleActor actor = new RemoveConditionalStyleActor(conditionalStyleModel, index);
		Controller.getCurrentModeController().execute(actor, map);
		return actor.item;
	}

	public TableModel getConditionalStyleModelAsTableModel(final MapModel map, final ConditionalStyleModel conditionalStyleModel) {
		return new TableModel() {
			private final TableModel tableModel = conditionalStyleModel.asTableModel();

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
	    filterComposerDialog.acceptMultipleConditions(true);
	    if(value != null)
	    	filterComposerDialog.addCondition(value);
	    filterComposerDialog.show();
	    List<ASelectableCondition> conditions = filterComposerDialog.getConditions();
	    if(filterComposerDialog.isSuccess())
	    	return conditions.isEmpty() ? null : conditions.get(0);
	    return value;
    }
    

}
