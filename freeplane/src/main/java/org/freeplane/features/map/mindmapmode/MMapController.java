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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
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
import java.util.WeakHashMap;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardControllers;
import org.freeplane.features.clipboard.mindmapmode.MClipboardControllers;
import org.freeplane.features.commandsearch.CommandSearchAction;
import org.freeplane.features.icon.mindmapmode.MIconController.Keys;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.AlwaysUnfoldedNode;
import org.freeplane.features.map.Clones;
import org.freeplane.features.map.DocuMapAttribute;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.FirstGroupNode;
import org.freeplane.features.map.FirstGroupNodeFlag;
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
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.features.map.SummaryLevels;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.map.mindmapmode.clipboard.MMapClipboardController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.styles.LogicalStyleKeys;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.styles.mindmapmode.NewNodeStyle;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.NodeAndMapReference;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.features.url.mindmapmode.MFileManager.AlternativeFileMode;
import org.freeplane.features.url.mindmapmode.MapLoader;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;

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
                @Override
                public void onSelect(final NodeModel node) {
                    final ViewController viewController = Controller.getCurrentController().getViewController();
                    if (ResourceController.getResourceController().getBooleanProperty("display_node_id")) {
                        viewController.addStatusInfo("display_node_id", "ID=" + node.createID(), null);
                    }
                }

                @Override
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
                    if(! nodeMoveEvent.oldParent.equals(nodeMoveEvent.newParent)) {
                        final NodeModel node = nodeMoveEvent.oldParent;
                        if (isSummaryNodeWithoutChildren(node)){
                            if(nodeMoveEvent.newParent == node.getParentNode())
                                SwingUtilities.invokeLater(() ->
                                    deleteSingleSummaryNode(node));
                            else
                                deleteSingleSummaryNode(node);
                        }
                    }
                }

                @Override
                public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
                }

                @Override
                public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
                    final NodeModel parent = nodeDeletionEvent.parent;
                    if (isSummaryNodeWithoutChildren(parent)){
                        deleteSingleSummaryNode(parent);
                    }
                }

                private boolean isSummaryNodeWithoutChildren(final NodeModel node) {
                    return !node.getMap().isUndoActionRunning() && ! node.isFolded() && ! node.hasChildren() && SummaryNode.isSummaryNode(node)&& node.getText().isEmpty();
                }

                @Override
                public void mapChanged(MapChangeEvent event) {
                }
            });

    }

    public NodeModel addNewNode(int newNodeMode) {
        KeyEvent currentKeyEvent = getCurrentKeyEvent();
        stopInlineEditing();
        final IMapSelection selection = Controller.getCurrentController().getSelection();
        final NodeModel targetNode = selection.getSelected();
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
                    final int index = childPosition;
                    newNode = addNewNode(parent, index, node -> {
                        node.setSide(targetNode.getSide());
                        NewNodeStyle.assignStyleToNewNode(node);
                    });
                    if (newNode == null) {
                        return null;
                    }
                    if(ResourceController.getResourceController().getBooleanProperty("copyFormatToNewSibling")) {
                        copyFormat(targetNode, newNode);
                    }
                    select(newNode);
                    startEditing(newNode, currentKeyEvent);
                    break;
                }
                else {
                    newNodeMode = MMapController.NEW_CHILD;
                }
            }
            //$FALL-THROUGH$
            case MMapController.NEW_CHILD: {
                final boolean targetFolded = isFolded(targetNode);
                Controller controller = getModeController().getController();
                IMapViewManager mapViewManager = controller.getMapViewManager();
                if (targetFolded) {
                    if(! targetNode.isRoot() && mapViewManager.hasHiddenChildren(targetNode.getParentNode())){
                        mapViewManager.hideChildren(targetNode);
                        targetNode.setFolded(false);
                    }
                    else
                        unfold(targetNode, controller.getSelection().getFilter());
                }
                final int position = findNewNodePosition(targetNode);
                Side newChildSide = targetNode.suggestNewChildSide(selection.getSelectionRoot());
                final Side side = newChildSide;
                newNode = addNewNode(targetNode, position, node -> {
                    node.setSide(side);
                    NewNodeStyle.assignStyleToNewNode(node);
                });
                if (newNode == null) {
                    return null;
                }
                if(ResourceController.getResourceController().getBooleanProperty("copyFormatToNewChild")) {
                    copyFormat(targetNode, newNode);
                }
                select(newNode);
                startEditing(newNode, currentKeyEvent);
                break;
            }
            default:
                newNode = null;
        }
        return newNode;
    }

    public int findNewNodePosition(final NodeModel targetNode) {
        boolean placeAsFirstChild = placesNewChildFirst(targetNode);
        IMapViewManager mapViewManager = getModeController().getController().getMapViewManager();
        final int position = placeAsFirstChild
                ? 0
                : targetNode.getChildCount() - mapViewManager.getHiddenChildCount(targetNode);
        return position;
    }

    public boolean placesNewChildFirst(final NodeModel targetNode) {
        final MapStyleModel mapStyleModel = MapStyleModel.getExtension(targetNode.getMap());
        MapViewLayout layoutType = mapStyleModel.getMapViewLayout();

        boolean placeAsFirstChild = layoutType ==  MapViewLayout.OUTLINE ||
            ResourceController.getResourceController().getProperty("placenewbranches").equals("first");
        return placeAsFirstChild;
    }

    private void copyFormat(final NodeModel source, final NodeModel target) {
        getMModeController().copyExtensions(LogicalStyleKeys.NODE_STYLE, source, target);
        getMModeController().copyExtensions(LogicalStyleKeys.LOGICAL_STYLE, source, target);
        if(ResourceController.getResourceController().getBooleanProperty("copyFormatToNewNodeIncludesIcons")) {
            getMModeController().copyExtensions(Keys.ICONS, source, target);
        }
    }



    @Override
    protected MapClipboardController createMapClipboardController() {
        final MMapClipboardController mapClipboardController = new MMapClipboardController(getMModeController());
        final MClipboardControllers extension = (MClipboardControllers) getModeController().getExtension(ClipboardControllers.class);
        extension.add(mapClipboardController);
        return mapClipboardController;
    }

    private void startEditing(final NodeModel newNode, KeyEvent currentKeyEvent) {
        final Component component = Controller.getCurrentController().getMapViewManager().getComponent(newNode);
        if(component == null)
            return;
        final TextController textController = TextController.getController();
        ((MTextController) textController).edit(newNode, newNode.getParentNode(), true, false, false, currentKeyEvent);
    }

    private KeyEvent getCurrentKeyEvent() {
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if(currentEvent instanceof KeyEvent)
            return (KeyEvent) currentEvent;
        else
            return null;
    }


    private void stopInlineEditing() {
        final TextController textController = TextController.getController();
        if (textController instanceof MTextController) {
            ((MTextController) textController).stopInlineEditing();
        }
        NoteController noteController = NoteController.getController();
        if (noteController instanceof MNoteController) {
            ((MNoteController) noteController).stopEditing();
        }
    }

    public void addNewSummaryNodeStartEditing(NodeModel selectionRoot, final NodeModel parentNode, final int start,
            final int end, final boolean isTopOrLeft) {
    	SummaryLevels summaryLevels = new SummaryLevels(selectionRoot, parentNode);
    	if(!summaryLevels.canInsertSummaryNode(start, end, isTopOrLeft))
    		return;
        ModeController modeController = getMModeController();
        stopInlineEditing();
        Side side = parentNode.getChildAt(end).getSide();
        final NodeModel newSummaryNode = addNewNode(parentNode, end+1, side);
        final SummaryNode summary = modeController.getExtension(SummaryNode.class);
        summary.undoableActivateHook(newSummaryNode, SUMMARY);
        AlwaysUnfoldedNode unfolded = modeController.getExtension(AlwaysUnfoldedNode.class);
        unfolded.undoableActivateHook(newSummaryNode, unfolded);
        final FirstGroupNode firstGroupNodeHook = modeController.getExtension(FirstGroupNode.class);
        final NodeModel startNode = parentNode.getChildAt(start);
        if(SummaryNode.isSummaryNode(startNode))
            firstGroupNodeHook.undoableActivateHook(startNode, FIRST_GROUP);
        else {
        	addNewFirstGroupNode(parentNode, start, newSummaryNode.getSide());
        }
        final NodeModel firstSummaryChildNode = addNewNode(newSummaryNode, 0, node -> {
            node.setSide(Side.DEFAULT);
            NewNodeStyle.assignStyleToNewNode(node);
        });
        KeyEvent currentKeyEvent = getCurrentKeyEvent();
        select(firstSummaryChildNode);
        startEditing(firstSummaryChildNode, currentKeyEvent);
    }

    private void addNewFirstGroupNode(final NodeModel parentNode, final int start,
            final Side side) {
        addNewNode(parentNode, start,
                n -> {
                    n.setSide(side);
                    n.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
                });
    }

    public NodeModel addNewNode(final NodeModel parent, final int index, final Side side) {
        return addNewNode(parent, index, node -> node.setSide(side));
    }

    public NodeModel addNewNode(final NodeModel parent, final int index, Consumer<NodeModel> configurator) {
        if (!isWriteable(parent)) {
            UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
            return null;
        }
        final NodeModel newNode = newNode("", parent.getMap());
        configurator.accept(newNode);
        if(addNewNode(newNode, parent, index))
            return newNode;
        else
            return null;
    }

    public boolean addNewNode(final NodeModel newNode, final NodeModel parent, final int index) {
        if (!isWriteable(parent)) {
            UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
            return false;
        }
        insertNewNode(newNode, parent, index);
        return true;
    }

    private void insertNewNode(final NodeModel newNode, final NodeModel parent, final int index) {
        if(index < 0 || index > parent.getChildCount()){
            insertNewNode(newNode, parent, parent.getChildCount());
            return;
        }
        if(newNode.subtreeContainsCloneOf(parent)){
            UITools.errorMessage("not allowed");
            return;
        }
        stopInlineEditing();
        insertSingleNewNode(newNode, parent, index);
        for(NodeModel parentClone : parent.subtreeClones()){
            if(parentClone != parent) {
                final NodeModel childClone = newNode.cloneTree();
                insertSingleNewNode(childClone, parentClone, index);
            }
        }
    }

    private void insertSingleNewNode(final NodeModel newNode, final NodeModel parent, final int index) {
        final MapModel map = parent.getMap();
        final IActor actor = new IActor() {
            @Override
            public void act() {
                insertNodeIntoWithoutUndo(newNode, parent, index);
            }

            @Override
            public String getDescription() {
                return "addNewNode";
            }

            @Override
            public void undo() {
                deleteWithoutUndo(parent, index);
            }
        };
        Controller.getCurrentModeController().execute(actor, map);
    }

    public boolean close(final MapModel map) {
        if (!(map.isSaved() || map.isReadOnly())) {
            Controller.getCurrentController().getMapViewManager().changeToMap(map);
            final String text = TextUtils.getText("save_unsaved") + "\n" + map.getTitle();
            final String title = TextUtils.getText("SaveAction.text");
            Component dialogParent;
            final Frame viewFrame = UITools.getCurrentFrame();
            if(viewFrame != null && viewFrame.isShowing() && viewFrame.getExtendedState() != Frame.ICONIFIED)
                dialogParent = viewFrame;
            else
                dialogParent = UITools.getCurrentRootComponent();
            final int returnVal = JOptionPane.showOptionDialog(dialogParent, text, title,
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (returnVal == JOptionPane.YES_OPTION) {
                final boolean savingNotCancelled = ((MFileManager) getModeController().getExtension(UrlManager.class))
                        .save(map);
                if (!savingNotCancelled) {
                    return false;
                }
            }
            else if ((returnVal == JOptionPane.CANCEL_OPTION) || (returnVal == JOptionPane.CLOSED_OPTION)) {
                return false;
            }
        }
        closeWithoutSaving(map);
        return true;
    }

    @Override
    synchronized public void closeWithoutSaving(final MapModel map) {
        loadedMaps.remove(map);
        super.closeWithoutSaving(map);
    }

    private void createActions(ModeController modeController) {
        modeController.addAction(new NewMapViewAction());
        modeController.addAction(new JumpInAction());
        modeController.addAction(new JumpOutAction());
        modeController.addAction(new NewSiblingAction());
        modeController.addAction(new NewPreviousSiblingAction());
        modeController.addAction(new NewChildAction());
        modeController.addAction(new NewSummaryAction());
        modeController.addAction(new NewFreeNodeAction());
        modeController.addAction(new DeleteAction());
        modeController.addAction(new NodeUpAction());
        modeController.addAction(new NodeDownAction());
        modeController.addAction(new ConvertCloneToIndependentNodeAction());
        modeController.addAction(new CommandSearchAction());
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
        final NodeModel duplicate = node.duplicate(false);
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
        final MModeController mModeController = getMModeController();
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
        final SummaryLevels summaryLevels = new SummaryLevels(summaryParent, summaryParent);
        final int summaryNodeIndex = summarynode.getIndex();
        final int groupBeginNodeIndex = summaryLevels.findGroupBeginNodeIndex(summaryNodeIndex - 1);
        deleteSingleNode(summaryParent, summaryNodeIndex);
        NodeModel groupBeginNode = summaryParent.getChildAt(groupBeginNodeIndex);
		if(SummaryNode.isFirstGroupNode(groupBeginNode)) {
			if(SummaryNode.isSummaryNode(groupBeginNode)) {
				 final FirstGroupNode firstGroupNodeHook = getModeController().getExtension(FirstGroupNode.class);
				 firstGroupNodeHook.undoableDeactivateHook(groupBeginNode);
			}
			else
				deleteSingleNode(summaryParent, groupBeginNodeIndex);
		}
    }

    private void deleteSingleNode(final NodeModel parentNode, final int index) {
        final NodeModel node = parentNode.getChildAt(index);
        final IActor actor = new IActor() {
            @Override
            public void act() {
                deleteWithoutUndo(parentNode, index);
            }

            @Override
            public String getDescription() {
                return "delete";
            }

            @Override
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
        return (MModeController) getModeController();
    }

    public void insertNode(final NodeModel node, final NodeModel parent) {
        insertNode(node, parent, findNewNodePosition(parent));
    }

    public void insertNode(final NodeModel node, final NodeModel target, final boolean asSibling) {
        NodeModel parent;
        if (asSibling) {
            parent = target.getParentNode();
        }
        else {
            parent = target;
        }
        if (asSibling) {
            insertNode(node, parent, parent.getIndex(target));
        }
        else {
            insertNode(node, parent, findNewNodePosition(target));
        }
    }

    public void insertNode(final NodeModel node, final NodeModel parentNode, final int index) {
        insertNewNode(node, parentNode, index);
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

    public void moveNodes(final List<NodeModel> movedNodes, final NodeModel newParent, final int newIndex) {
        final List<NodeModel> movedNodesWithSummaryGroupIndicators = new SummaryGroupEdgeListAdder(movedNodes).addSummaryEdgeNodes();
        int index = newIndex;
        for(NodeModel node : movedNodesWithSummaryGroupIndicators) {
            final NodeModel oldParent = node.getParentNode();
            if(newParent.subtreeClones().contains(oldParent)){
                final NodeModel childNode = node;
                final int oldIndex = oldParent.getIndex(childNode);
                if(oldIndex < newIndex) {
                    index--;
                }
            }
            moveNodeAndItsClones(node, newParent, index++);
        }
        balanceFirstGroupNodes(newParent);
    }

    public void setSide(List<NodeModel> nodes, Side side) {
    	final List<NodeModel> movedNodesWithSummaryGroupIndicators = new SummaryGroupEdgeListAdder(nodes).addSummaryEdgeNodes();
    	for(NodeModel node : movedNodesWithSummaryGroupIndicators) {
    		Side oldSide = node.getSide();
    		if(oldSide != side) {
    			IActor sideChangeActor = new IActor() {

    				@Override
    				public void act() {
    					node.setSide(side);
    					delayedNodeRefresh(node, Side.class, oldSide, side);
    				}
    				@Override
    				public void undo() {
    					node.setSide(oldSide);
    					delayedNodeRefresh(node, Side.class, side, oldSide);
    				}

    				@Override
    				public String getDescription() {
    					return "change side";
    				}

    			};
    			getModeController().execute(sideChangeActor, node.getMap());

    		}
    	}
    	nodes.stream().map(NodeModel::getParentNode)
    		.distinct()
    		.forEach(this::balanceFirstGroupNodes);
    }


    public void moveNodeAndItsClones(NodeModel child, final NodeModel newParent, int newIndex) {
        if(child.subtreeContainsCloneOf(newParent)){
            UITools.errorMessage("not allowed");
            return;
        }
        final NodeModel oldParent = child.getParentNode();
        if(newParent != oldParent && newParent.subtreeClones().contains(oldParent)) {
            moveNodeAndItsClones(child, oldParent, newIndex);
            return;
        }

        final int oldIndex = oldParent.getIndex(child);
        final int childCount = newParent.getChildCount();
        newIndex = newIndex >= childCount ? oldParent == newParent ? childCount - 1 : childCount : newIndex;


        if(oldParent != newParent) {
        	Side newSide;
        	if(oldParent.isHiddenSummary()) {
        		newSide = oldParent.getSide();
        	}
        	else {
        		Side oldSide = child.isTopOrLeft(newParent) ? Side.TOP_OR_LEFT : Side.BOTTOM_OR_RIGHT;
        		newSide = MapController.suggestNewChildSide(newParent, oldSide);
        	}
        	setSide(Collections.singletonList(child), newSide);
		}

        if (oldParent != newParent || oldIndex != newIndex) {
            final NodeRelativePath nodeRelativePath = getPathToNearestTargetClone(oldParent, newParent);

            final Set<NodeModel> oldParentClones = new HashSet<NodeModel>(oldParent.subtreeClones().toCollection());
            final Set<NodeModel> newParentClones = new HashSet<NodeModel>(newParent.subtreeClones().toCollection());

            final NodeModel commonAncestor = nodeRelativePath.commonAncestor();
            for (NodeModel commonAncestorClone: commonAncestor.subtreeClones()){
                    NodeModel oldParentClone = nodeRelativePath.pathBegin(commonAncestorClone);
                    NodeModel newParentClone = nodeRelativePath.pathEnd(commonAncestorClone);
                    moveSingleNode(oldParentClone.getChildAt(oldIndex), newParentClone, newIndex);
                    oldParentClones.remove(oldParentClone);
                    newParentClones.remove(newParentClone);
            }

            for(NodeModel newParentClone : newParentClones)
                insertSingleNewNode(child.cloneTree(), newParentClone, newIndex);

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

    private void moveSingleNode(final NodeModel child, final NodeModel newParent, final int newIndex) {
        final NodeModel oldParent = child.getParentNode();
        final int oldIndex = oldParent.getIndex(child);

        final IActor actor = new IActor() {
            @Override
            public void act() {
                moveNodeToWithoutUndo(child, newParent, newIndex);
            }

            @Override
            public String getDescription() {
                return "moveNode";
            }

            @Override
            public void undo() {
                moveNodeToWithoutUndo(child, oldParent, oldIndex);
            }
        };
        Controller.getCurrentModeController().execute(actor, newParent.getMap());
    }

    public void moveNodesAsChildren(final List<NodeModel> children, final NodeModel target) {
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
        moveNodes(children, target, position);
    }

    public void moveNodesBefore(final List<NodeModel> children, final NodeModel target) {
        final NodeModel newParent = target.getParentNode();
        int newIndex = newParent.getIndex(target);
        for(NodeModel node : children){
            Controller.getCurrentModeController().getExtension(FreeNode.class).undoableDeactivateHook(node);
        }
        moveNodes(children, newParent, newIndex);
    }

    public void moveNodesInGivenDirection(NodeModel selectionRoot, NodeModel selected, Collection<NodeModel> movedNodes, final int direction) {
        final List<NodeModel> movedNodesWithEdges = new SummaryGroupEdgeListAdder(movedNodes).addSummaryEdgeNodes();
        final Collection<NodeModel> movedNodeSet = new HashSet<NodeModel>(movedNodesWithEdges);

        final Comparator<Object> comparator = (direction == -1) ? null : new Comparator<Object>() {
            @Override
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
            final List<NodeModel> sortedChildren = getSiblingsSortedOnSide(selectionRoot, parent);
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
            final int maxIndex = parent.getChildCount();
            boolean movesGroupDown = direction == 1 && SummaryNode.isFirstGroupNode(movedNodesWithEdges.get(0));
            for (final Integer position : range) {
                final NodeModel node = sortedChildren.get(position.intValue());
                final List<NodeModel> sortedOnSideNodes = getSiblingsSortedOnSide(selectionRoot, parent);
                int newPositionInVector = sortedOnSideNodes.indexOf(node) + direction;
                if (newPositionInVector < 0) {
                    newPositionInVector = maxIndex - 1;
                }
                if (newPositionInVector >= maxIndex) {
                    newPositionInVector = 0;
                }
                final NodeModel destinationNode = sortedOnSideNodes.get(newPositionInVector);
                int newIndex = parent.getIndex(destinationNode)
                        + ((movesGroupDown && SummaryNode.isFirstGroupNode(destinationNode)) ? 1 : 0);
                moveNodeAndItsClones(node, parent, newIndex);
            }
            final IMapSelection selection = Controller.getCurrentController().getSelection();
            selection.selectAsTheOnlyOneSelected(selected);
            for (NodeModel selectedNode : selectedNodes) {
                selection.makeTheSelected(selectedNode);
            }
            balanceFirstGroupNodes(parent);
        }
    }

    /**
     * Sorts nodes by their left/right status. The left are first.
     * @param selectionRoot TODO
     */
    private List<NodeModel> getSiblingsSortedOnSide(NodeModel selectionRoot, final NodeModel node) {
        final ArrayList<NodeModel> nodes = new ArrayList<NodeModel>(node.getChildCount());
        for (final NodeModel child : node.getChildren()) {
            nodes.add(child);
        }
        if(node != selectionRoot && ! node.isRoot()){
            return nodes;
        }
        final MapStyleModel mapStyleModel = MapStyleModel.getExtension(node.getMap());
        MapViewLayout layoutType = mapStyleModel.getMapViewLayout();
        if(layoutType.equals(MapViewLayout.OUTLINE)){
            return nodes;
        }

        Collections.sort(nodes, new Comparator<Object>() {
            @Override
            public int compare(final Object o1, final Object o2) {
                if (o1 instanceof NodeModel) {
                    final NodeModel n1 = (NodeModel) o1;
                    if (o2 instanceof NodeModel) {
                        final NodeModel n2 = (NodeModel) o2;
                        final int b1 = n1.isTopOrLeft(selectionRoot) ? 0 : 1;
                        final int b2 = n2.isTopOrLeft(selectionRoot) ? 0 : 1;
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
    private int moveNodeToWithoutUndo(final NodeModel child, final NodeModel newParent, int newIndex) {
        final NodeModel oldParent = child.getParentNode();
        final int oldIndex = oldParent.getIndex(child);
        final NodeMoveEvent nodeMoveEvent = new NodeMoveEvent(oldParent, oldIndex, newParent, child, newIndex);
        firePreNodeMoved(nodeMoveEvent);
        oldParent.remove(oldParent.getIndex(child));
        newParent.insert(child, newIndex);
        fireNodeMoved(nodeMoveEvent);
        setSaved(newParent.getMap(), false);
        return newIndex;
    }

    public void balanceFirstGroupNodes(final NodeModel parent) {
        removeSuperficiousFirstGroupNodes(parent);
        addMissingFirstGroupNodes(parent);
    }

    private void removeSuperficiousFirstGroupNodes(final NodeModel parent) {
        NodeModel leftFirstGroupNode = null;
        NodeModel rightFirstGroupNode = null;
        IMapSelection selection = Controller.getCurrentController().getSelection();
        boolean shouldConsiderSeparateSides = selection != null && selection.getSelectionRoot() == parent || parent.isRoot();
        for(int i = 0; i < parent.getChildCount(); i++) {
            NodeModel child = parent.getChildAt(i);
            boolean isFirstGroupNode = SummaryNode.isFirstGroupNode(child);
            boolean isSummaryNode = SummaryNode.isSummaryNode(child);
            if(isFirstGroupNode != isSummaryNode) {
                boolean isTopOrLeft = shouldConsiderSeparateSides && child.isTopOrLeft(parent);
                if(isTopOrLeft) {
                    if(isFirstGroupNode) {
                        if(leftFirstGroupNode != null) {
                            deleteSingleNodeWithClones(child);
                        }
                        leftFirstGroupNode = child;
                    }
                    else
                        leftFirstGroupNode = null;
                }
                else if(isFirstGroupNode) {
                    if(rightFirstGroupNode != null) {
                        deleteSingleNodeWithClones(child);
                    }
                    rightFirstGroupNode = child;
                }
                else
                    rightFirstGroupNode = null;
            }
        }
    }
    private void addMissingFirstGroupNodes(final NodeModel parent) {
        boolean isLeftItemNodeFound = false;
        boolean isRightItemNodeFound = false;
        NodeModel leftSummaryNode = null;
        NodeModel rightSummaryNode = null;
        IMapSelection selection = Controller.getCurrentController().getSelection();
        boolean shouldConsiderSeparateSides = selection != null && selection.getSelectionRoot() == parent || parent.isRoot();
        for(int i = parent.getChildCount() - 1; i >= 0; i--) {
            NodeModel child = parent.getChildAt(i);
            boolean isFirstGroupNode = SummaryNode.isFirstGroupNode(child);
            boolean isSummaryNode = SummaryNode.isSummaryNode(child);
            boolean isTopOrLeft = shouldConsiderSeparateSides && child.isTopOrLeft(parent);
            if(isFirstGroupNode != isSummaryNode) {
                if(isTopOrLeft) {
                    if(isSummaryNode) {
                        if(leftSummaryNode != null && isLeftItemNodeFound) {
                            addNewFirstGroupNode(parent, i+1, leftSummaryNode.getSide());
                        }
                        leftSummaryNode = child;
                        isLeftItemNodeFound = false;
                    } else {
                        leftSummaryNode = null;
                    }
                }
                else if(isSummaryNode) {
                    if(rightSummaryNode != null && isRightItemNodeFound) {
                        addNewFirstGroupNode(parent, i+1, rightSummaryNode.getSide());
                    }
                } else {
                    rightSummaryNode = null;
                }
            }
            if(isSummaryNode) {
                if(isTopOrLeft) {
                    leftSummaryNode = child;
                    isLeftItemNodeFound = false;
                } else {
                    rightSummaryNode = child;
                    isRightItemNodeFound = false;
                }
            }
            if(! isFirstGroupNode && ! isSummaryNode) {
                if(isTopOrLeft)
                    isLeftItemNodeFound = true;
                else
                    isRightItemNodeFound = true;
            }
        }
    }

    public MapModel createModel(NodeModel existingNode) {
        // use new MMapModel() instead of calling this method with a null arg
        if(existingNode == null)
            throw new NullPointerException("null node not allowed.");
        final MMapModel mindMapMapModel = new MMapModel(duplicator());
        mindMapMapModel.setRoot(existingNode);
        mindMapMapModel.registryNodeRecursive(existingNode);
        fireMapCreated(mindMapMapModel);
        return mindMapMapModel;
    }

    @Override
    public MapModel newMap() {
        return MFileManager.getController(getModeController()).newMapFromDefaultTemplate();
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
                saveAction.setEnabled(UserRole.EDITOR);
        }
    }

    public NodeModel addFreeNode(final NodeModel target, final Point pt, final Side side) {
        final ModeController modeController = Controller.getCurrentModeController();
        final TextController textController = TextController.getController();
        if (textController instanceof MTextController) {
            ((MTextController) textController).stopInlineEditing();
                modeController.forceNewTransaction();
        }
        final boolean parentFolded = isFolded(target);
        if (parentFolded) {
            unfold(target, modeController.getController().getSelection().getFilter());
        }
        if (!isWriteable(target)) {
            UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
            return null;
        }
        final NodeModel newNode = newNode("", target.getMap());
        newNode.setSide(side);
        LogicalStyleModel.createExtension(newNode).setStyle(MapStyleModel.FLOATING_STYLE);
        newNode.addExtension(modeController.getExtension(FreeNode.class));
        if(! addNewNode(newNode, target, target.getChildCount()))
            return null;
        final Quantity<LengthUnit> x = LengthUnit.pixelsInPt(pt.x);
        final Quantity<LengthUnit> y = LengthUnit.pixelsInPt(pt.y);
        ((MLocationController)MLocationController.getController(modeController)).moveNodePosition(newNode, x, y);
        final Component component = Controller.getCurrentController().getMapViewManager().getComponent(newNode);
        if (component == null)
            return newNode;
        component.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                e.getComponent().removeFocusListener(this);
                ((MTextController) textController).edit(newNode, target, true, false, false);
            }
        });
        select(newNode);
        return newNode;
    }

    /**@param follow
     * @deprecated -- use MMapIO*/
    @Deprecated
    public MapModel newMap(final URL url, boolean follow){
        return new MapLoader(getMModeController()).load(url).unsetMapLocation(follow).withView().getMap();
    }

    private WeakHashMap<MMapModel, Void> loadedMaps = new WeakHashMap<>();


    synchronized public MMapModel getMap(URL url) {
        for(MMapModel hiddenMap : loadedMaps.keySet()) {
            if(url.equals(hiddenMap.getURL()))
                return hiddenMap;
        }
        return null;
    }


    synchronized public void addLoadedMap(MMapModel map) {
        loadedMaps.put(map, null);
    }


    public MMapModel createUntitledMap(final URL url, boolean follow) throws IOException, XMLException {
        return (MMapModel) new MapLoader(getMModeController()).load(url).unsetMapLocation(follow).withView().getMap();
    }

    public MapModel readMap(URL url) throws FileNotFoundException, XMLParseException, IOException, URISyntaxException {
        return new MapLoader(getMModeController()).load(url).getMap();
    }

    /**@throws XMLException
     * @deprecated -- use MMapIO*/
    @Deprecated
    @Override
    public void openMap(URL url) {
        if (AddOnsController.getController().installIfAppropriate(url))
            return;
        new MapLoader(getMModeController()).load(url).withView().getMap();
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
                    openDocumentationMap(endUrl);
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
    public void openDocumentationMap(final URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException{
        new MapLoader(getMModeController()).load(url).withView().asDocumentation().getMap();
    }

    public void restoreCurrentMap() throws FileNotFoundException, IOException, URISyntaxException, XMLException {
        restoreCurrentMap(true);
    }

    public void restoreCurrentMapIgnoreAlternatives() throws FileNotFoundException, IOException, URISyntaxException, XMLException {
        restoreCurrentMap(false);
    }

    private void restoreCurrentMap(boolean checkAlternatives) throws FileNotFoundException, IOException, URISyntaxException, XMLException {
        final Controller controller = Controller.getCurrentController();
        final MapModel map = controller.getMap();
        final URL url = map.getURL();
        if(url == null){
            UITools.errorMessage(TextUtils.getText("map_not_saved"));
            return;
        }

        if(map.containsExtension(DocuMapAttribute.class)){
            closeWithoutSaving(map);
            openDocumentationMap(url);
            return;
        }

        final URL alternativeURL = checkAlternatives ? MFileManager.getController(getMModeController()).getAlternativeURL(url, AlternativeFileMode.ALL) : url;
        if(alternativeURL == null)
            return;
        controller.getViewController().setWaitingCursor(true);
        try{
            map.releaseResources();
            final MMapModel newModel = new MMapModel(duplicator());
            ((MFileManager)MFileManager.getController()).loadAndLock(alternativeURL, newModel);
            newModel.setURL(url);
            newModel.setSaved(alternativeURL.equals(url));
            fireMapCreated(newModel);
            addLoadedMap(newModel);
            closeWithoutSaving(map);
            newModel.enableAutosave();
            createMapView(newModel);
            return;
        }
        finally {
            controller.getViewController().setWaitingCursor(false);
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
                public boolean isReadonly() {
                    return true;
                }

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

    public <T extends IExtension> void setProperty(NodeModel node, T property) {
        final Class<? extends IExtension> propertyClass = property.getClass();
        setProperty(node, propertyClass, property);
    }

    public <T extends IExtension> void removeProperty(NodeModel node, Class<T> propertyClass) {
        setProperty(node, propertyClass, null);
    }

    public <T extends IExtension> void setProperty(NodeModel node, final Class<? extends IExtension> propertyClass,
                                                    T property) {
        final IExtension oldProperty = node.getExtension(propertyClass);
        if(oldProperty != property) {
            IActor actor = new IActor() {
                @Override
                public void undo() {
                    node.putExtension(propertyClass, oldProperty);
                    nodeChanged(node, propertyClass, property, oldProperty);
                }

                @Override
                public String getDescription() {
                    return "setProperty";
                }

                @Override
                public void act() {
                    node.putExtension(propertyClass, property);
                    nodeChanged(node, propertyClass, oldProperty, property);
                }
            };
            getModeController().execute(actor, node.getMap());
        }
    }

}
