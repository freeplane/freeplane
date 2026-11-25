/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.map.clipboard.MindMapNodesSelection;
import org.freeplane.features.map.mindmapmode.InsertionRelation;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.clipboard.MMapClipboardController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MainView.DragOverDirection;
import org.freeplane.view.swing.map.MainView.DragOverRelation;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewIconListComponent;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.NodeViewFolder;
import org.freeplane.view.swing.ui.MouseEventActor;
import org.freeplane.view.swing.ui.NodeDropUtils;

public class MNodeDropListener implements DropTargetListener {

	private static final String PROPERTY_UNFOLD_ON_PASTE = "unfold_on_paste";
	static private final EnumMap<DragOverRelation, Side> sides = new EnumMap<>(DragOverRelation.class);
	static {
		sides.put(DragOverRelation.SIBLING_AFTER, Side.AS_SIBLING_AFTER);
		sides.put(DragOverRelation.SIBLING_BEFORE, Side.AS_SIBLING_BEFORE);
	}
	static private final EnumMap<DragOverRelation, InsertionRelation> insertionRelations = new EnumMap<>(DragOverRelation.class);
	private final NodeViewFolder nodeFolder;
	static {
		insertionRelations.put(DragOverRelation.SIBLING_AFTER, InsertionRelation.AS_SIBLING_AFTER);
		insertionRelations.put(DragOverRelation.SIBLING_BEFORE, InsertionRelation.AS_SIBLING_BEFORE);
	}


// 	final private ModeController modeController;

	MNodeDropListener(NodeViewFolder nodeFolder) {
		this.nodeFolder = nodeFolder;
	}


	public void addDropListener(MainView component) {
		addDropListener((Component)component);
	}

	public void addDropListener(MapViewIconListComponent component) {
		addDropListener((Component)component);
	}

	private void addDropListener(Component component) {
		final DropTarget dropTarget = new DropTarget(component, this);
		dropTarget.setActive(true);
	}

	/**
	 * The method is called when the cursor carrying the dragged item enteres
	 * the area of the node. The name "dragEnter" seems to be confusing to me. I
	 * think the difference between dragAcceptable and dropAcceptable is that in
	 * dragAcceptable, you tell if the type of the thing being dragged is OK,
	 * where in dropAcceptable, you tell if your really willing to accept the
	 * item.
	 */
	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
		if (isDragAcceptable(dtde)) {
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);

		}
		else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragExit(final DropTargetEvent e) {
		final MainView mainView = getMainView(e);
		mainView.stopDragOver();
		mainView.repaint();
		if(isInFoldingRegion(mainView)) {
			NodeView nodeView = mainView.getNodeView();
			nodeFolder.adjustFolding(Collections.singleton(nodeView));
		}

	}

	private boolean isInFoldingRegion(final MainView node) {
	    AWTEvent currentEvent = EventQueue.getCurrentEvent();
	    if(! (currentEvent instanceof MouseEvent))
	    	return false;
	    MouseEvent mouseEvent = ((MouseEvent)currentEvent);
	    if(mouseEvent.getComponent() != node)
	    	return false;
	    Point p = mouseEvent.getPoint();
		final DragOverDirection dragOverDirection;
		if(p.x < 0 && p.y >= 0 && p.y <node.getHeight())
		    dragOverDirection = DragOverDirection.DROP_LEFT;
		else if (p.x >= node.getWidth() && p.y >= 0 && p.y <node.getHeight())
		    dragOverDirection = DragOverDirection.DROP_RIGHT;
		else if (p.y < 0 && p.x >= 0 && p.x < node.getWidth())
		    dragOverDirection = DragOverDirection.DROP_UP;
		else if (p.y >= node.getHeight() && p.x >= 0 && p.x < node.getWidth())
		    dragOverDirection = DragOverDirection.DROP_DOWN;
		else return false;

		NodeView nodeView = node.getNodeView();
		DragOverRelation relation = dragOverDirection.relation(nodeView.layoutOrientation(), nodeView.side());
		return relation.isChild() && nodeView.childrenSides().matches(relation == DragOverRelation.CHILD_BEFORE);
	}

    private MainView getMainView(final DropTargetEvent e) {
	    DropTargetContext dropTargetContext = e.getDropTargetContext();
		return getMainView(dropTargetContext);
    }

	private MainView getMainView(DropTargetContext dropTargetContext) {
		final Component component = dropTargetContext.getComponent();
		if(component instanceof MainView)
			return (MainView) component;
		NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, component);
		return nodeView.getMainView();
	}

	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
		if(isDragAcceptable(dtde)) {
			final MainView dropTarget = getMainView(dtde.getDropTargetContext());
			dropTarget.setDragOverDirection(dtde);
		}
	}

	private boolean isDragAcceptable(final DropTargetDragEvent event) {
		NodeView nodeView = getMainView(event.getDropTargetContext()).getNodeView();
		NodeDropUtils.AcceptedContent acceptedContent = determineAcceptedContent(event.getDropTargetContext());
		return NodeDropUtils.isDragAcceptable(event, nodeView.getNode(), acceptedContent);
	}

	private NodeDropUtils.AcceptedContent determineAcceptedContent(DropTargetContext context) {
		Component component = context.getComponent();
		if (component instanceof MainView) {
			return NodeDropUtils.AcceptedContent.ANY;
		} else if (component instanceof MapViewIconListComponent) {
			return NodeDropUtils.AcceptedContent.ONLY_TAGS;
		} else {
			return NodeDropUtils.AcceptedContent.ANY;
		}
	}

	private boolean isDropAcceptable(final DropTargetDropEvent event, int dropAction) {
		final NodeModel node = getMainView(event.getDropTargetContext()).getNodeView().getNode();
		NodeDropUtils.AcceptedContent acceptedContent = determineAcceptedContent(event.getDropTargetContext());
		return NodeDropUtils.isDropAcceptable(event, node, dropAction, acceptedContent);
	}

	@Override
	public void drop(final DropTargetDropEvent dtde) {
		try {
			DropContext context = prepareDropContext(dtde);

			if (!validateDrop(dtde, context)) {
				dtde.rejectDrop();
				return;
			}

			if (isExternalDrop(dtde)) {
				handleExternalDrop(dtde, context);
			} else if (isLinkAction(context.dropAction)) {
				handleLinkAction(dtde, context);
			} else if (isLocalNodeMove(dtde, context)) {
				handleLocalNodeMove(dtde, context);
			} else {
				handleClipboardContentDrop(dtde, context);
			}

			adjustFoldingOnDrop(context);
		}
		catch (final Exception e) {
			LogUtils.severe("Drop exception:", e);
			dtde.dropComplete(false);
			return;
		}
		dtde.dropComplete(true);
	}

	private static class DropContext {
		final MainView mainView;
		final NodeView targetNodeView;
		final MapView mapView;
		final NodeModel targetNode;
		final NodeModel targetParentNode;
		final Controller controller;
		final ModeController modeController;
		final MMapController mapController;
		final int dropAction;
		final Transferable transferable;
		final DragOverRelation dragOverRelation;
		final boolean dropAsSibling;
		final boolean isTopOrLeft;
		final Side side;
		final InsertionRelation insertionRelation;

		DropContext(DropTargetDropEvent dtde, MNodeDropListener listener) {
			this.mainView = listener.getMainView(dtde.getDropTargetContext());
			this.targetNodeView = mainView.getNodeView();
			this.mapView = targetNodeView.getMap();
			this.targetNode = targetNodeView.getNode();
			this.targetParentNode = targetNode.getParentNode();
			this.controller = Controller.getCurrentController();
			this.modeController = controller.getModeController();
			this.mapController = (MMapController) modeController.getMapController();
			this.dropAction = NodeDropUtils.getDropAction(dtde);
			this.transferable = dtde.getTransferable();
			this.dragOverRelation = mainView.dragOverRelation(dtde);
			this.dropAsSibling = dragOverRelation.isSibling();
			this.isTopOrLeft = dragOverRelation == DragOverRelation.CHILD_BEFORE;
			this.side = dropAsSibling ? sides.get(dragOverRelation) : isTopOrLeft ? Side.TOP_OR_LEFT : Side.BOTTOM_OR_RIGHT;
			this.insertionRelation = insertionRelations.getOrDefault(dragOverRelation, InsertionRelation.AS_CHILD);
		}
	}

	private DropContext prepareDropContext(final DropTargetDropEvent dtde) {
		DropContext context = new DropContext(dtde, this);
		context.mapView.select();
		context.mainView.stopDragOver();
		context.mainView.repaint();
		return context;
	}

	private boolean validateDrop(final DropTargetDropEvent dtde, DropContext context) {
		if (!isDropAcceptable(dtde, context.dropAction)) {
			return false;
		}

		if (context.dragOverRelation == DragOverRelation.NOT_AVAILABLE) {
			return false;
		}

		if (requiresWriteableParent(context.dropAction)) {
			final NodeModel parent = context.dropAsSibling ? context.targetNode.getParentNode() : context.targetNode;
			if (!context.mapController.isWriteable(parent)) {
				final String message = TextUtils.getText("node_is_write_protected");
				UITools.errorMessage(message);
				return false;
			}
		}

		return true;
	}

	private boolean requiresWriteableParent(int dropAction) {
		return dropAction == DnDConstants.ACTION_MOVE || dropAction == DnDConstants.ACTION_COPY;
	}

	private boolean isExternalDrop(final DropTargetDropEvent dtde) {
		return !dtde.isLocalTransfer();
	}

	private boolean isLinkAction(int dropAction) {
		return dropAction == DnDConstants.ACTION_LINK;
	}

	private boolean isLocalNodeMove(final DropTargetDropEvent dtde, DropContext context) {
		if (context.dropAction != DnDConstants.ACTION_MOVE || !dtde.isLocalTransfer()) {
			return false;
		}

		if (!context.transferable.isDataFlavorSupported(MindMapNodesSelection.mindMapNodeObjectsFlavor)) {
			return false;
		}

		try {
			return NodeDropUtils.areFromSameMap(context.transferable, context.targetNode);
		} catch (UnsupportedFlavorException | IOException e) {
			return false;
		}
	}

	private void handleExternalDrop(final DropTargetDropEvent dtde, DropContext context) throws Exception {
		adjustFoldingOnDrop(context);
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		NodeDropUtils.handleMoveOrCopyAction(context.transferable, context.targetNode,
			context.dropAction, false, context.insertionRelation, context.side);
		MouseEventActor.INSTANCE.withMouseEvent(() ->
		context.controller.getSelection().selectAsTheOnlyOneSelected(liveTargetNode(context)));
	}

	private void handleLinkAction(final DropTargetDropEvent dtde, DropContext context) throws Exception {
		dtde.acceptDrop(context.dropAction);
		NodeDropUtils.handleLinkAction(context.transferable, context.targetNode,
			context.controller, context.modeController);
	}

	private void handleLocalNodeMove(final DropTargetDropEvent dtde, DropContext context) throws Exception {
		dtde.acceptDrop(context.dropAction);

		final Collection<NodeModel> selectedNodes = NodeDropUtils.getNodeObjects(context.transferable);
		final NodeModel[] selectedArray = selectedNodes.toArray(new NodeModel[selectedNodes.size()]);

		NodeDropUtils.handleMoveOrCopyAction(context.transferable, context.targetNode,
			context.dropAction, true, context.insertionRelation, context.side);

		updateSelectionAfterNodeMove(context, selectedArray);
	}

	private void updateSelectionAfterNodeMove(DropContext context, NodeModel[] selectedArray) {
		if (context.dropAsSibling || !context.targetNodeView.isFolded()) {
			MouseEventActor.INSTANCE.withMouseEvent(() ->
				context.mapView.getMapSelection().replaceSelection(selectedArray));
		} else {
			MouseEventActor.INSTANCE.withMouseEvent(() ->
				context.mapView.selectAsTheOnlyOneSelected(context.targetNodeView));
		}
	}

	private void handleClipboardContentDrop(final DropTargetDropEvent dtde, DropContext context) throws Exception {
		dtde.acceptDrop(context.dropAction);
		((MMapClipboardController) MapClipboardController.getController())
			.paste(context.transferable, context.targetNode, context.side, context.dropAction);
		MouseEventActor.INSTANCE.withMouseEvent(() ->
			context.controller.getSelection().selectAsTheOnlyOneSelected(liveTargetNode(context)));
	}

	private NodeModel liveTargetNode(final DropContext context) {
		if (context.targetNode.getParentNode() != null || context.targetParentNode == null)
			return context.targetNode;
		return context.targetParentNode.getChildCount() > 0 ? context.targetParentNode.getChildAt(0) : context.targetParentNode;
	}


	private void adjustFoldingOnDrop(final DropContext context) {
		final NodeView targetNodeView = context.targetNodeView;
		final DragOverRelation dragOverRelation = context.dragOverRelation;
		boolean unfoldsTarget = ResourceController.getResourceController().getBooleanProperty(PROPERTY_UNFOLD_ON_PASTE);
		Set<NodeView> nodesKeptUnfold;
		if(unfoldsTarget) {
			final boolean targetWasReplaced = context.targetNode.getParentNode() == null && context.targetParentNode != null;
			if (targetWasReplaced) {
				final NodeView parentNodeView = context.mapView.getNodeView(context.targetParentNode);
				nodesKeptUnfold = parentNodeView != null ? Collections.singleton(parentNodeView) : Collections.<NodeView>emptySet();
			} else if (dragOverRelation.isChild()) {
				nodesKeptUnfold = Collections.singleton(targetNodeView);
			} else {
				NodeView parentNodeView = targetNodeView.getAncestorWithVisibleContent();
				nodesKeptUnfold = parentNodeView != null ? Collections.singleton(parentNodeView) : Collections.<NodeView>emptySet();
			}
		} else {
			nodesKeptUnfold = Collections.emptySet();
		}
		nodeFolder.adjustFolding(nodesKeptUnfold);
		nodeFolder.reset();
	}



	@Override
	public void dropActionChanged(final DropTargetDragEvent e) {
	}

}
