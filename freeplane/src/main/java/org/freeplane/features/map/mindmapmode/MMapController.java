/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.map.mindmapmode;

import static org.freeplane.features.map.FirstGroupNodeFlag.FIRST_GROUP;
import static org.freeplane.features.map.SummaryNodeFlag.SUMMARY;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.Quantity;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.icon.mindmapmode.MIconController.Keys;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.AlwaysUnfoldedNode;
import org.freeplane.features.map.Clones;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.FirstGroupNode;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.features.map.SummaryLevels;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.styles.LogicalStyleKeys;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.NodeAndMapReference;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.features.url.mindmapmode.MFileManager.AlternativeFileMode;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.n3.nanoxml.XMLException;

/**
 * @author Dimitry Polivaev
 */
public class MMapController extends MapController {
	public static final int NEW_CHILD = 2;
	public static final int NEW_SIBLING_BEFORE = 4;
	public static final int NEW_SIBLING_BEHIND = 3;
	public static final String RESOURCES_CONVERT_TO_CURRENT_VERSION = "convert_to_current_version";

	public MMapController(ModeController modeController) {
		super(modeController);
		createActions(modeController);
			addNodeSelectionListener(new INodeSelectionListener() {
				public void onSelect(final NodeModel node) {
					final ViewController viewController = Controller.getCurrentController().getViewController();
					if (ResourceController.getResourceController().getBooleanProperty("display_node_id")) {
						viewController.addStatusInfo("display_node_id", "ID=" + node.createID(), null);
					}
				}

				public void onDeselect(final NodeModel node) {
					final ViewController viewController = Controller.getCurrentController().getViewController();
					viewController.addStatusInfo("display_node_id", null, null);
				}
			});
			addMapChangeListener(new IMapChangeListener() {
				
				@Override
				public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
				}
				
				@Override
				public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
				}
				
				@Override
				public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
					if(! nodeMoveEvent.oldParent.equals(nodeMoveEvent.newParent))
						onNodeDeleted(nodeMoveEvent.oldParent);
				}
				
				@Override
				public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
				}
				
				@Override
				public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
					final NodeModel parent = nodeDeletionEvent.parent;
					onNodeDeleted(parent);
				}

				private void onNodeDeleted(final NodeModel node) {
					if (!getModeController().isUndoAction() && ! node.isFolded() && ! node.hasChildren() && SummaryNode.isSummaryNode(node)&& node.getText().isEmpty()){
						deleteSingleSummaryNode(node);
					}
				}
				
				@Override
				public void mapChanged(MapChangeEvent event) {
				}
			});
	
	}

	public NodeModel addNewNode(int newNodeMode) {
		stopEditing();
		final NodeModel targetNode = getSelectedNode();
		final NodeModel newNode;
		switch (newNodeMode) {
			case MMapController.NEW_SIBLING_BEFORE:
			case MMapController.NEW_SIBLING_BEHIND: {
				if (!targetNode.isRoot()) {
					final NodeModel parent = targetNode.getParentNode();
					int childPosition = parent.getIndex(targetNode);
					if (newNodeMode == MMapController.NEW_SIBLING_BEHIND) {
						childPosition++;
					}
					newNode = addNewNode(parent, childPosition, targetNode.isLeft());
					if (newNode == null) {
						return null;
					}
					if(ResourceController.getResourceController().getBooleanProperty("copyFormatToNewSibling")) {
						getMModeController().undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, targetNode, newNode);
						getMModeController().undoableCopyExtensions(LogicalStyleKeys.LOGICAL_STYLE, targetNode, newNode);
						if(ResourceController.getResourceController().getBooleanProperty("copyFormatToNewSiblingIncludesIcons")) {
							getMModeController().undoableCopyExtensions(Keys.ICONS, targetNode, newNode);
						}
					}
					startEditingAfterSelect(newNode);
					select(newNode);
					break;
				}
				else {
					newNodeMode = MMapController.NEW_CHILD;
				}
			}
			//$FALL-THROUGH$
			case MMapController.NEW_CHILD: {
				final boolean parentFolded = isFolded(targetNode);
				if (parentFolded) {
					setFolded(targetNode, false);
				}
				final int position = ResourceController.getResourceController().getProperty("placenewbranches").equals(
				    "last") ? targetNode.getChildCount() : 0;
				newNode = addNewNode(targetNode, position, targetNode.isNewChildLeft());
				if (newNode == null) {
					return null;
				}
				startEditingAfterSelect(newNode);
				select(newNode);
				break;
			}
			default:
				newNode = null;
		}
		return newNode;
	}

	private void startEditingAfterSelect(final NodeModel newNode) {
		final Component component = Controller.getCurrentController().getMapViewManager().getComponent(newNode);
		if(component == null)
			return;
		component.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				e.getComponent().removeFocusListener(this);
				final TextController textController = TextController.getController();
				((MTextController) textController).edit(newNode, newNode.getParentNode(), true, false, false);
			}
		});
    }

	private void stopEditing() {
		final TextController textController = TextController.getController();
		if (textController instanceof MTextController) {
			((MTextController) textController).stopEditing();
		}
    }

	public void addNewSummaryNodeStartEditing(final NodeModel parentNode, final int start, final int end,
			final int summaryLevel, final boolean isLeft) {
		ModeController modeController = getMModeController();
		stopEditing();
		final NodeModel newSummaryNode = addNewNode(parentNode, end+1, isLeft);
		final SummaryNode summary = modeController.getExtension(SummaryNode.class);
		summary.undoableActivateHook(newSummaryNode, SUMMARY);
		AlwaysUnfoldedNode unfolded = modeController.getExtension(AlwaysUnfoldedNode.class);
		unfolded.undoableActivateHook(newSummaryNode, unfolded);
		final FirstGroupNode firstGroupNodeHook = modeController.getExtension(FirstGroupNode.class);
		final NodeModel firstNodeInGroup = parentNode.getChildAt(start);
		if(SummaryNode.isSummaryNode(firstNodeInGroup))
			firstGroupNodeHook.undoableActivateHook(firstNodeInGroup, FIRST_GROUP);
		else {
			final NodeModel previousNode = firstNodeInGroup.previousNode(start, isLeft);
			if(previousNode == null || SummaryNode.isSummaryNode(previousNode) || !SummaryNode.isFirstGroupNode(previousNode)) {
				NodeModel newFirstGroup = addNewNode(parentNode, start, isLeft);
				firstGroupNodeHook.undoableActivateHook(newFirstGroup, FIRST_GROUP);
			}
			firstGroupNodeHook.undoableDeactivateHook(firstNodeInGroup);
		}
		int level = summaryLevel;
		for(int i = start+1; i <= end; i++){
			NodeModel node = parentNode.getChildAt(i);
			if(isLeft != node.isLeft())
				continue;
			if(SummaryNode.isSummaryNode(node))
				level++;
			else
				level = 0;
			if(level == summaryLevel && SummaryNode.isFirstGroupNode(node)){
				if(level > 0)
					firstGroupNodeHook.undoableDeactivateHook(node);
				else
					deleteSingleNodeWithClones(node);
			}
		}
		final NodeModel firstSummaryChildNode = addNewNode(newSummaryNode, 0, isLeft);
		startEditingAfterSelect(firstSummaryChildNode);
		select(firstSummaryChildNode);
	}

	public NodeModel addNewNode(final NodeModel parent, final int index, final boolean newNodeIsLeft) {
		if (!isWriteable(parent)) {
			UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
			return null;
		}
		final NodeModel newNode = newNode("", parent.getMap());
		if(addNewNode(newNode, parent, index, newNodeIsLeft))
			return newNode;
		else
			return null;
	}

	public boolean addNewNode(final NodeModel newNode, final NodeModel parent, final int index, final boolean newNodeIsLeft) {
		if (!isWriteable(parent)) {
			UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
			return false;
		}
		insertNewNode(newNode, parent, index, newNodeIsLeft);
		return true;
    }

	private void insertNewNode(final NodeModel newNode, final NodeModel parent, final int index,
                               final boolean newNodeIsLeft) {
		if(index < 0 || index > parent.getChildCount()){
			insertNewNode(newNode, parent, parent.getChildCount(), newNodeIsLeft);
			return;
		}
		if(newNode.subtreeContainsCloneOf(parent)){
			UITools.errorMessage("not allowed");
			return;
		}
		stopEditing();
		insertSingleNewNode(newNode, parent, index, newNodeIsLeft);
		for(NodeModel parentClone : parent.subtreeClones()){
			if(parentClone != parent) {
				final NodeModel childClone = newNode.cloneTree();
				insertSingleNewNode(childClone, parentClone, index, parentClone.isLeft());
            }
		}
    }

	private void insertSingleNewNode(final NodeModel newNode, final NodeModel parent, final int index,
                                  final boolean newNodeIsLeft) {
	    final MapModel map = parent.getMap();
		newNode.setLeft(newNodeIsLeft);
		final IActor actor = new IActor() {
			public void act() {
				insertNodeIntoWithoutUndo(newNode, parent, index);
			}

			public String getDescription() {
				return "addNewNode";
			}

			public void undo() {
				deleteWithoutUndo(parent, index);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
    }

	/**
	 * Return false if user has canceled.
	 */
	@Override
	public boolean close(final MapModel map, final boolean force) {
		if (!force && !map.isSaved()) {
			final List<Component> views = Controller.getCurrentController().getMapViewManager().getViews(map);
			if (views.size() == 1) {
				final String text = TextUtils.getText("save_unsaved") + "\n" + map.getTitle();
				final String title = TextUtils.getText("SaveAction.text");
				Component dialogParent;
				final Frame viewFrame = JOptionPane.getFrameForComponent(views.get(0));
				if(viewFrame != null && viewFrame.isShowing() && viewFrame.getExtendedState() != Frame.ICONIFIED)
					dialogParent = viewFrame;
				else
					dialogParent = UITools.getCurrentRootComponent();
				final int returnVal = JOptionPane.showOptionDialog(dialogParent, text, title,
				    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
				if (returnVal == JOptionPane.YES_OPTION) {
					final boolean savingNotCancelled = ((MFileManager) UrlManager.getController())
					    .save(map);
					if (!savingNotCancelled) {
						return false;
					}
				}
				else if ((returnVal == JOptionPane.CANCEL_OPTION) || (returnVal == JOptionPane.CLOSED_OPTION)) {
					return false;
				}
			}
		}
		return super.close(map, force);
	}

	private void createActions(ModeController modeController) {
		modeController.addAction(new NewMapViewAction());
		modeController.addAction(new NewSiblingAction());
		modeController.addAction(new NewPreviousSiblingAction());
		modeController.addAction(new NewChildAction());
		modeController.addAction(new NewSummaryAction());
		modeController.addAction(new NewFreeNodeAction());
		modeController.addAction(new DeleteAction());
		modeController.addAction(new NodeUpAction());
		modeController.addAction(new NodeDownAction());
		modeController.addAction(new ConvertCloneToIndependentNodeAction());
	}

	public void deleteNode(NodeModel node) {
		deleteNodes(Arrays.asList(node));
	}
	
	public void deleteNodes(final List<NodeModel> nodes) {
		final List<NodeModel> deletedNodesWithSummaryGroupIndicators = new SummaryGroupEdgeListAdder(nodes).addSummaryEdgeNodes();
		for(NodeModel node : deletedNodesWithSummaryGroupIndicators){
		    deleteSingleNodeWithClones(node);
		}
	}
	
	public void convertClonesToIndependentNodes(final NodeModel node){
		final MLinkController linkController = (MLinkController) MLinkController.getController();
		if(node.isCloneTreeRoot()){
			linkController.deleteMapLinksForClone(node);
			convertCloneToNode(node);
			linkController.insertMapLinksForClone(node);
		}
	}

	private void convertCloneToNode(final NodeModel node) {
		final MModeController mModeController = getMModeController();
		final ClipboardController clipboardController = mModeController.getExtension(ClipboardController.class);
		final NodeModel duplicate = clipboardController.duplicate(node, false);
		IActor converter = new IActor() {
			
			@Override
			public void act() {
				node.swapData(duplicate);
				nodeChanged(node);
			}
			
			@Override
			public void undo() {
				node.swapData(duplicate);
				nodeChanged(node);
			}
			
			@Override
			public String getDescription() {
				return "convertClonesToIndependentNodes";
			}
			
		};
		final boolean shouldConvertChildNodes = node.subtreeClones().size() > 1;
		mModeController.execute(converter, node.getMap());
		if(shouldConvertChildNodes)
			for (NodeModel child : node.getChildren())
				convertCloneToNode(child);
	}

	private void deleteSingleNodeWithClones(NodeModel node) {
		final NodeModel parentNode = node.getParentNode();
		final int index = parentNode.getIndex(node);
		for(NodeModel parentClone : parentNode.subtreeClones())
			deleteSingleNode(parentClone, index);
	}

	private void deleteSingleSummaryNode(NodeModel summarynode) {
		final NodeModel summaryParent = summarynode.getParentNode();
		final SummaryLevels summaryLevels = new SummaryLevels(summaryParent);
		final int summaryNodeIndex = summarynode.getIndex();
		final int groupBeginNodeIndex = summaryLevels.findGroupBeginNodeIndex(summaryNodeIndex - 1);
		deleteSingleNode(summaryParent, summaryNodeIndex);
		deleteSingleNode(summaryParent, groupBeginNodeIndex);
	}

	private void deleteSingleNode(final NodeModel parentNode, final int index) {
		final NodeModel node = parentNode.getChildAt(index);
		final IActor actor = new IActor() {
        	public void act() {
        		deleteWithoutUndo(parentNode, index);
        	}

        	public String getDescription() {
        		return "delete";
        	}

        	public void undo() {
				(Controller.getCurrentModeController().getMapController()).insertNodeIntoWithoutUndo(node, parentNode, index);
        	}
        };
		Controller.getCurrentModeController().execute(actor, parentNode.getMap());
    }

	private void deleteWithoutUndo(final NodeModel parent, final int index) {
	    final NodeModel child = parent.getChildAt(index);
	    final NodeDeletionEvent nodeDeletionEvent = new NodeDeletionEvent(parent, child, index);
		firePreNodeDelete(nodeDeletionEvent);
		final MapModel map = parent.getMap();
		setSaved(map, false);
		parent.remove(index);
		fireNodeDeleted(nodeDeletionEvent);
    }

	public MModeController getMModeController() {
		return (MModeController) Controller.getCurrentModeController();
	}

	public void insertNode(final NodeModel node, final NodeModel parent) {
		insertNode(node, parent, parent.getChildCount());
	}

	public void insertNode(final NodeModel node, final NodeModel target, final boolean asSibling, final boolean isLeft,
	                       final boolean changeSide) {
		NodeModel parent;
		if (asSibling) {
			parent = target.getParentNode();
		}
		else {
			parent = target;
		}
		if (changeSide) {
			node.setParent(parent);
			node.setLeft(isLeft);
		}
		if (asSibling) {
			insertNode(node, parent, parent.getIndex(target));
		}
		else {
			insertNode(node, parent, parent.getChildCount());
		}
	}

	public void insertNode(final NodeModel node, final NodeModel parentNode, final int index) {
		insertNewNode(node, parentNode, index, node.isLeft());
	}

	@Override
	public void insertNodeIntoWithoutUndo(final NodeModel newNode, final NodeModel parent, final int index) {
		setSaved(parent.getMap(), false);
		super.insertNodeIntoWithoutUndo(newNode, parent, index);
	}

	public boolean isWriteable(final NodeModel targetNode) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(targetNode);
		if (encryptionModel != null) {
			return encryptionModel.isAccessible();
		}
		return true;
	}

	public void moveNode(NodeModel node, int newIndex) {
		moveNodes(Arrays.asList(node), node.getParentNode(), newIndex);
	}

	public void moveNodes(final List<NodeModel> children, final NodeModel newParent, final int newIndex) {
		moveNodes(children, newParent, newIndex, false, false);
	}

	public void moveNodes(final List<NodeModel> movedNodes, final NodeModel newParent, final int newIndex, final boolean isLeft,
	                     final boolean changeSide) {
		final List<NodeModel> movedNodesWithSummaryGroupIndicators = new SummaryGroupEdgeListAdder(movedNodes).addSummaryEdgeNodes();
		int index = newIndex;
		for(NodeModel node : movedNodesWithSummaryGroupIndicators)
			moveNodeAndItsClones(node, newParent, index++, isLeft, changeSide && node.isLeft() != isLeft);
	}

	public void moveNodeAndItsClones(NodeModel child, final NodeModel newParent, int newIndex, final boolean isLeft,
			final boolean changeSide) {
		if(child.subtreeContainsCloneOf(newParent)){
			UITools.errorMessage("not allowed");
			return;
		}
		final NodeModel oldParent = child.getParentNode();
		if(newParent != oldParent && newParent.subtreeClones().contains(oldParent)) {
			moveNodeAndItsClones(child, oldParent, newIndex, newParent.isLeft(), false);
			return;
		}
			
		final NodeModel childNode = child;
		final int oldIndex = oldParent.getIndex(childNode);
		final int childCount = newParent.getChildCount();
		newIndex = newIndex >= childCount ? oldParent == newParent ? childCount - 1 : childCount : newIndex;

		if (oldParent != newParent || oldIndex != newIndex || changeSide != false) {
			final NodeRelativePath nodeRelativePath = getPathToNearestTargetClone(oldParent, newParent);

			final Set<NodeModel> oldParentClones = new HashSet<NodeModel>(oldParent.subtreeClones().toCollection());
			final Set<NodeModel> newParentClones = new HashSet<NodeModel>(newParent.subtreeClones().toCollection());

			final NodeModel commonAncestor = nodeRelativePath.commonAncestor();
			for (NodeModel commonAncestorClone: commonAncestor.subtreeClones()){
					NodeModel oldParentClone = nodeRelativePath.pathBegin(commonAncestorClone);
					NodeModel newParentClone = nodeRelativePath.pathEnd(commonAncestorClone);
					final boolean isLeftForClone = newParentClone == newParent ? isLeft : newParentClone.isLeft();
					moveSingleNode(oldParentClone.getChildAt(oldIndex), newParentClone, newIndex, isLeftForClone, changeSide);
					oldParentClones.remove(oldParentClone);
					newParentClones.remove(newParentClone);
			}

			for(NodeModel newParentClone : newParentClones)
				insertSingleNewNode(child.cloneTree(), newParentClone, newIndex, newParentClone.isLeft());

			for(NodeModel oldParentClone : oldParentClones)
					deleteSingleNode(oldParentClone, oldIndex);
		}
	}

	private NodeRelativePath getPathToNearestTargetClone(final NodeModel source, final NodeModel target) {
		if(source == target)
			return new NodeRelativePath(source, target);
		final Clones targetClones = target.subtreeClones();
		final int pathNumber = targetClones.size();
		if(pathNumber == 1)
			return new NodeRelativePath(source, target);
		Collection<NodeRelativePath> paths = new ArrayList<>(pathNumber);
		for(NodeModel targetClone : targetClones)
			paths.add(new NodeRelativePath(source, targetClone));
		final NodeRelativePath shortestPath = Collections.min(paths, new Comparator<NodeRelativePath>() {
			@Override
			public int compare(NodeRelativePath o1, NodeRelativePath o2) {
				return o1.getPathLength() - o2.getPathLength();
			}
		});
		return shortestPath;
	}

	private void moveSingleNode(final NodeModel child, final NodeModel newParent, final int newIndex,
                                final boolean isLeft, final boolean changeSide) {
		final NodeModel oldParent = child.getParentNode();
		final int oldIndex = oldParent.getIndex(child);
		
		final boolean wasLeft = child.isLeft();
		final IActor actor = new IActor() {
			public void act() {
				moveNodeToWithoutUndo(child, newParent, newIndex, isLeft, changeSide);
			}

			public String getDescription() {
				return "moveNode";
			}

			public void undo() {
				moveNodeToWithoutUndo(child, oldParent, oldIndex, wasLeft, changeSide);
			}
		};
		Controller.getCurrentModeController().execute(actor, newParent.getMap());
    }

	public void moveNodesAsChildren(final List<NodeModel> children, final NodeModel target, final boolean isLeft,
	                            final boolean changeSide) {
		FreeNode r = Controller.getCurrentModeController().getExtension(FreeNode.class);
		for(NodeModel node : children){
			final IExtension extension = node.getExtension(FreeNode.class);
			if (extension != null) {
				r.undoableToggleHook(node, extension);
				if (MapStyleModel.FLOATING_STYLE.equals(LogicalStyleModel.getStyle(node)))
					((MLogicalStyleController)MLogicalStyleController.getController(getMModeController())).setStyle(node, null);
			}
		}
		int position = target.getChildCount();
		moveNodes(children, target, position, isLeft, changeSide);
	}

	public void moveNodesBefore(final List<NodeModel> children, final NodeModel target, final boolean isLeft,
	                           final boolean changeSide) {
        final NodeModel newParent = target.getParentNode();
        int newIndex = newParent.getIndex(target);
        for(NodeModel node : children){
        	final NodeModel oldParent = node.getParentNode();
        	if(newParent.subtreeClones().contains(oldParent)){
        		final NodeModel childNode = node;
				final int oldIndex = oldParent.getIndex(childNode);
        		if(oldIndex < newIndex)
        			newIndex--;
        	}
        	Controller.getCurrentModeController().getExtension(FreeNode.class).undoableDeactivateHook(node);
        }
        moveNodes(children, newParent, newIndex, isLeft, changeSide);
	}

	public void moveNodesInGivenDirection(NodeModel selected, Collection<NodeModel> movedNodes, final int direction) {
		final List<NodeModel> movedNodesWithEdges = new SummaryGroupEdgeListAdder(movedNodes).addSummaryEdgeNodes();
		final Collection<NodeModel> movedNodeSet = new HashSet<NodeModel>(movedNodesWithEdges);
		
        final Comparator<Object> comparator = (direction == -1) ? null : new Comparator<Object>() {
            public int compare(final Object o1, final Object o2) {
                final int i1 = ((Integer) o1).intValue();
                final int i2 = ((Integer) o2).intValue();
                return i2 - i1;
            }
        };
		if (movedNodeSet.size() == 0)
			return;
		final NodeModel oneMovedNode = movedNodeSet.iterator().next();
		final NodeModel parent = oneMovedNode.getParentNode();
        if (parent != null) {
            final List<NodeModel> sortedChildren = getSiblingsSortedOnSide(parent);
            final TreeSet<Integer> range = new TreeSet<Integer>(comparator);
            for (final NodeModel node : movedNodeSet) {
                if (node.getParentNode() != parent) {
                    LogUtils.warn("Not all selected nodes have the same parent.");
                    return;
                }
                range.add(new Integer(sortedChildren.indexOf(node)));
            }
            Integer last = range.iterator().next();
            for (final Integer newInt : range) {
                if (Math.abs(newInt.intValue() - last.intValue()) > 1) {
                    LogUtils.warn("Not adjacent nodes. Skipped. ");
                    return;
                }
                last = newInt;
            }
            Collection<NodeModel> selectedNodes = new ArrayList<NodeModel>(getSelectedNodes());
            for (final Integer position : range) {
                final NodeModel node = sortedChildren.get(position.intValue());
                moveSingleNodeInGivenDirection(node, direction);
            }
            final IMapSelection selection = Controller.getCurrentController().getSelection();
            selection.selectAsTheOnlyOneSelected(selected);
			for (NodeModel selectedNode : selectedNodes) {
				selection.makeTheSelected(selectedNode);
            }
        }
    }

	private int moveSingleNodeInGivenDirection(final NodeModel child, final int direction) {
        final NodeModel parent = child.getParentNode();
        final int index = parent.getIndex(child);
        int newIndex = index;
        final int maxIndex = parent.getChildCount();
        final List<NodeModel> sortedOnSideNodes = getSiblingsSortedOnSide(parent);
        int newPositionInVector = sortedOnSideNodes.indexOf(child) + direction;
        if (newPositionInVector < 0) {
            newPositionInVector = maxIndex - 1;
        }
        if (newPositionInVector >= maxIndex) {
            newPositionInVector = 0;
        }
        final NodeModel destinationNode = sortedOnSideNodes.get(newPositionInVector);
        newIndex = parent.getIndex(destinationNode);
        moveNodeAndItsClones(child, parent, newIndex, child.isLeft(),false);
        return newIndex;
    }
    /**
     * Sorts nodes by their left/right status. The left are first.
     */
    private List<NodeModel> getSiblingsSortedOnSide(final NodeModel node) {
        final ArrayList<NodeModel> nodes = new ArrayList<NodeModel>(node.getChildCount());
        for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
            nodes.add(child);
        }
        if(! node.isRoot()){
            return nodes;
        }
        final MapStyleModel mapStyleModel = MapStyleModel.getExtension(node.getMap());
        MapViewLayout layoutType = mapStyleModel.getMapViewLayout();
        if(layoutType.equals(MapViewLayout.OUTLINE)){
            return nodes;
        }

        Collections.sort(nodes, new Comparator<Object>() {
            public int compare(final Object o1, final Object o2) {
                if (o1 instanceof NodeModel) {
                    final NodeModel n1 = (NodeModel) o1;
                    if (o2 instanceof NodeModel) {
                        final NodeModel n2 = (NodeModel) o2;
                        final int b1 = n1.isLeft() ? 0 : 1;
                        final int b2 = n2.isLeft() ? 0 : 1;
                        return b1 - b2;
                    }
                }
                throw new IllegalArgumentException("Elements in LeftRightComparator are not comparable.");
            }
        });
        return nodes;
    }

	/**
	 * The direction is used if side left and right are present. then the next
	 * suitable place on the same side# is searched. if there is no such place,
	 * then the side is changed.
	 *
	 * @return returns the new index.
	 */
	private int moveNodeToWithoutUndo(final NodeModel child, final NodeModel newParent, int newIndex,
	                          final boolean isLeft, final boolean changeSide) {
		final NodeModel oldParent = child.getParentNode();
		final int oldIndex = oldParent.getIndex(child);
		final boolean oldSideLeft = child.isLeft();
		final boolean newSideLeft = changeSide ? isLeft : oldSideLeft;
		final NodeMoveEvent nodeMoveEvent = new NodeMoveEvent(oldParent, oldIndex, oldSideLeft, newParent, child, newIndex, newSideLeft);
		firePreNodeMoved(nodeMoveEvent);
		oldParent.remove(oldParent.getIndex(child));
		if (changeSide) {
			child.setParent(newParent);
			child.setLeft(isLeft);
		}
		newParent.insert(child, newIndex);
		fireNodeMoved(nodeMoveEvent);
		setSaved(newParent.getMap(), false);
		return newIndex;
	}

	public MapModel newModel(NodeModel existingNode) {
	    // use new MMapModel() instead of calling this method with a null arg
		if(existingNode == null)
			throw new NullPointerException("null node not allowed.");
		final MMapModel mindMapMapModel = new MMapModel();
		mindMapMapModel.setRoot(existingNode);
		mindMapMapModel.registryNodeRecursive(existingNode);
		fireMapCreated(mindMapMapModel);
		return mindMapMapModel;
    }

	@Override
	public MapModel newModel() {
		final MMapModel mindMapMapModel = new MMapModel();
		mindMapMapModel.createNewRoot();
		fireMapCreated(mindMapMapModel);
		return mindMapMapModel;
	}


	@Override
    public void setSaved(final MapModel mapModel, final boolean saved) {
		final boolean setTitle = saved != mapModel.isSaved() || mapModel.isReadOnly();
		mapModel.setSaved(saved);
		if (setTitle) {
			final Controller controller = Controller.getCurrentController();
			controller.getMapViewManager().setMapTitles();
			final AFreeplaneAction saveAction = controller.getModeController().getAction("SaveAction");
			if(saveAction != null)
				saveAction.setEnabled();
		}
	}

	public NodeModel addFreeNode(final Point pt, final boolean newNodeIsLeft) {
		final ModeController modeController = Controller.getCurrentModeController();
		final TextController textController = TextController.getController();
		if (textController instanceof MTextController) {
			((MTextController) textController).stopEditing();
				modeController.forceNewTransaction();
		}
		final NodeModel target = getRootNode();
		final NodeModel targetNode = target;
		final boolean parentFolded = isFolded(targetNode);
		if (parentFolded) {
			setFolded(targetNode, false);
		}
		if (!isWriteable(target)) {
			UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
			return null;
		}
		final NodeModel newNode = newNode("", target.getMap());
		LogicalStyleModel.createExtension(newNode).setStyle(MapStyleModel.FLOATING_STYLE);
		newNode.addExtension(modeController.getExtension(FreeNode.class));
		if(! addNewNode(newNode, target, target.getChildCount(), newNodeIsLeft))
			return null;
		final Quantity<LengthUnits> x = LengthUnits.pixelsInPt(pt.x);
		final Quantity<LengthUnits> y = LengthUnits.pixelsInPt(pt.y);
		((MLocationController)MLocationController.getController(modeController)).moveNodePosition(newNode, x, y);
		final Component component = Controller.getCurrentController().getMapViewManager().getComponent(newNode);
		if (component == null)
			return newNode;
		component.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				e.getComponent().removeFocusListener(this);
				((MTextController) textController).edit(newNode, targetNode, true, false, false);
			}
		});
		select(newNode);
		return newNode;
	}

	/**@deprecated -- use MMapIO*/
	@Deprecated
	public boolean newUntitledMap(final URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException{
        try {
        	Controller.getCurrentController().getViewController().setWaitingCursor(true);
        	final MapModel newModel = new MMapModel();
        	UrlManager.getController().load(url, newModel);
        	newModel.setURL(null);
        	fireMapCreated(newModel);
        	newMapView(newModel);
        	return true;
        }
        finally {
        	Controller.getCurrentController().getViewController().setWaitingCursor(false);
        }
	}

	/**@throws XMLException
	 * @deprecated -- use MMapIO*/
	@Deprecated
	@Override
    public boolean newMap(URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException {
		// load as documentation map if necessary
		if(getMModeController().containsExtension(DocuMapAttribute.class)){
			return newDocumentationMap(url);
		}
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		if (mapViewManager.tryToChangeToMapView(url))
			return false;
		if (AddOnsController.getController().installIfAppropriate(url))
			return false;
		URL alternativeURL = null;
		try {
			final File file = Compat.urlToFile(url);
			if(file == null){
				alternativeURL =  url;
			}
			else{
				if(file.exists()){
					final MFileManager fileManager = MFileManager.getController(getMModeController());
					File alternativeFile = fileManager.getAlternativeFile(file, AlternativeFileMode.AUTOSAVE);
					if(alternativeFile != null){
						if (alternativeFile.getAbsoluteFile().equals(file.getAbsoluteFile()) )
							alternativeURL =  url;
						else
							alternativeURL = Compat.fileToUrl(alternativeFile);
					}
					else
						return false;
				}
				else{
					alternativeURL = url;
				}
			}
		}
		catch (MalformedURLException e) {
		}
		catch (URISyntaxException e) {
		}

		if(alternativeURL == null)
			return false;
		Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try{
			final MapModel newModel = new MMapModel();
    		final MFileManager fileManager = MFileManager.getController(getMModeController());
    		fileManager.loadAndLock(alternativeURL, newModel);
			newModel.setURL(url);
			newModel.setSaved(alternativeURL.equals(url));
			fireMapCreated(newModel);
			newMapView(newModel);
			return true;
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
    }
	
	public void newDocumentationMap(final String file) {
		final NodeAndMapReference nodeAndMapReference = new NodeAndMapReference(file);
		final ResourceController resourceController = ResourceController.getResourceController();
		final File userDir = new File(resourceController.getFreeplaneUserDirectory());
		final File baseDir = new File(resourceController.getInstallationBaseDir());
		final String languageCode = resourceController.getLanguageCode();
		final File localFile = ConfigurationUtils.getLocalizedFile(new File[]{userDir, baseDir}, nodeAndMapReference.getMapReference(), languageCode);
		if(localFile == null){
			String errorMessage = TextUtils.format("invalid_file_msg", file);
			UITools.errorMessage(errorMessage);
			return;
		}
		try {
			final URL endUrl = localFile.toURL();
			try {
				if (endUrl.getFile().endsWith(".mm")) {
					Controller.getCurrentController().selectMode(MModeController.MODENAME);
					newDocumentationMap(endUrl);
					if(nodeAndMapReference.hasNodeReference())
						select(nodeAndMapReference.getNodeReference());

				}
				else {
					Controller.getCurrentController().getViewController().openDocument(endUrl);
				}
			}
			catch (final Exception e1) {
				LogUtils.severe(e1);
			}
		}
		catch (final MalformedURLException e1) {
			LogUtils.warn(e1);
		}
	}
	


	/**@throws XMLException
	 * @deprecated -- use MMapIO*/
	@Deprecated
	public boolean newDocumentationMap(final URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException{
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		if (mapViewManager.tryToChangeToMapView(url))
			return false;
        try {
        	Controller.getCurrentController().getViewController().setWaitingCursor(true);
        	final MapModel newModel = new MMapModel();
        	newModel.addExtension(DocuMapAttribute.instance);
        	UrlManager.getController().load(url, newModel);
        	newModel.setReadOnly(true);
        	fireMapCreated(newModel);
        	newMapView(newModel);
        	newModel.setSaved(true);
        	return true;
        }
        finally {
        	Controller.getCurrentController().getViewController().setWaitingCursor(false);
        }
	}

	/**@throws XMLException
	 * @deprecated -- use MMapIO*/
	@Deprecated
    public boolean restoreCurrentMap() throws FileNotFoundException, IOException, URISyntaxException, XMLException {
	    final Controller controller = Controller.getCurrentController();
        final MapModel map = controller.getMap();
        final URL url = map.getURL();
        if(url == null){
        	UITools.errorMessage(TextUtils.getText("map_not_saved"));
        	return false;
        }

		if(map.containsExtension(DocuMapAttribute.class)){
			controller.close(true);
			return newDocumentationMap(url);
		}

		final URL alternativeURL = MFileManager.getController(getMModeController()).getAlternativeURL(url, AlternativeFileMode.ALL);
		if(alternativeURL == null)
			return false;
		Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try{
			final MapModel newModel = new MMapModel();
			((MFileManager)MFileManager.getController()).loadAndLock(alternativeURL, newModel);
			newModel.setURL(url);
			newModel.setSaved(alternativeURL.equals(url));
			fireMapCreated(newModel);
			controller.close(true);
			newMapView(newModel);
			return true;
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}
	
	@Override
	protected void setFoldingState(final NodeModel node, final boolean folded) {
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		final boolean wasFolded = mapViewManager.isFoldedOnCurrentView(node);
		if(wasFolded == folded && mapViewManager.getComponent(node) == null)
			return;
		if(isFoldingPersistent()){
			IActor foldingActor = new IActor() {
				@Override
				public void undo() {
					unfoldHiddenChildren(node);
					MMapController.super.setFoldingState(node, wasFolded);
				}
				
				@Override
				public String getDescription() {
					return "setFoldingState";
				}
				
				@Override
				public void act() {
					unfoldHiddenChildren(node);
					MMapController.super.setFoldingState(node, folded);
				}
			};
			getMModeController().execute(foldingActor, node.getMap());
		}
		else
			super.setFoldingState(node, folded);
	}
	
	static private final List<String> foldingSavedOptions = Arrays.asList(NodeBuilder.RESOURCES_ALWAYS_SAVE_FOLDING, NodeBuilder.RESOURCES_SAVE_FOLDING_IF_MAP_IS_CHANGED);
	
	private boolean isFoldingPersistent() {
	    final ResourceController resourceController = ResourceController.getResourceController();
		return foldingSavedOptions.contains(resourceController.getProperty(NodeBuilder.RESOURCES_SAVE_FOLDING));
	}

}
