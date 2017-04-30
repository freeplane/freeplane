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
import org.freeplane.core.ui.menubuilders.generic.ChildActionEntryRemover;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.filter.FilterComposerDialog;
import org.freeplane.features.filter.FilterController;
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
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
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
import org.freeplane.features.styles.SetBooleanMapPropertyAction;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.view.swing.features.filepreview.MapBackgroundClearAction;
import org.freeplane.view.swing.features.filepreview.MapBackgroundImageAction;

/**
 * @author Dimitry Polivaev
 * 28.09.2009
 */
public class MLogicalStyleController extends LogicalStyleController {
	private static final String STYLE_ACTIONS = "styleActions";

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
			modeController.addUiBuilder(Phase.ACTIONS, "style_actions", new StyleMenuBuilder(modeController),
			    new ChildActionEntryRemover(modeController));
			final IUserInputListenerFactory userInputListenerFactory = modeController.getUserInputListenerFactory();
			Controller.getCurrentController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
				public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
				}

				public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
					userInputListenerFactory.rebuildMenus(STYLE_ACTIONS);
				}
			});
			final MapController mapController = modeController.getMapController();
			mapController.addMapChangeListener(new IMapChangeListener() {
				public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
				}

				public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
				}

				public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
				}

				public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
				}

				public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
				}

				public void mapChanged(final MapChangeEvent event) {
					if (event.getProperty().equals(MapStyle.MAP_STYLES)) {
						userInputListenerFactory.rebuildMenus(STYLE_ACTIONS);
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

	class StyleMenuBuilder implements EntryVisitor {
		private final ModeController modeController;
		
		public StyleMenuBuilder(ModeController modeController) {
			super();
			this.modeController = modeController;
		}

		@Override
		public void visit(Entry target) {
			addStyleMenu(target);
		}

		@Override
		public boolean shouldSkipChildren(Entry entry) {
			return true;
		}

		private void addStyleMenu(final Entry target) {
			MapModel map = Controller.getCurrentController().getMap();
			if (map == null) {
				return;
			}
			final MapStyleModel extension = MapStyleModel.getExtension(map);
			if (extension == null) {
				return;
			}
			actions.clear();
			final NodeModel rootNode = extension.getStyleMap().getRootNode();
			final AssignStyleAction resetAction = new AssignStyleAction(null);
			modeController.addActionIfNotAlreadySet(resetAction);
			actions.add(resetAction);
			new EntryAccessor().addChildAction(target, resetAction);
			addStyleMenu(target, rootNode, extension);
		}

		private void addStyleMenu(final Entry target, final NodeModel styleMapNode, MapStyleModel extension) {
			final List<NodeModel> children = styleMapNode.getChildren();
			final EntryAccessor entryAccessor = new EntryAccessor();
			for (final NodeModel child : children) {
				if (child.hasChildren()) {
					addStyleMenu(target, child, extension);
				}
				else {
					Object userObject = child.getUserObject();
					if (userObject instanceof IStyle) {
						final IStyle style = (IStyle) userObject;
						if (null != extension.getStyleNode(style)) {
							final AssignStyleAction action = new AssignStyleAction(style);
							modeController.addActionIfNotAlreadySet(action);
							actions.add(action);
							entryAccessor.addChildAction(target, action);
						}
					}
					else
						LogUtils.severe("unexpected user object on style map: " + userObject);
				}
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
    

}
