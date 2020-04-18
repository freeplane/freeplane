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

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.map.clipboard.MindMapNodesSelection;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.clipboard.MMapClipboardController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MouseArea;
import org.freeplane.view.swing.map.NodeView;

public class MNodeDropListener implements DropTargetListener {
private static final int UNFOLD_DELAY_MILLISECONDS = 500;
private Timer timer;

// 	final private ModeController modeController;

	public MNodeDropListener() {
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
			supportFolding(dtde);
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		}
		else {
			dtde.rejectDrag();
		}
	}

	private void supportFolding(final DropTargetDragEvent dtde) {
		final MainView node = getNode(dtde);
		if(isInFoldingRegion(dtde)){
			node.setMouseArea(MouseArea.FOLDING);
			startUnfoldTimer(node);
		}
		else{
			node.setMouseArea(MouseArea.DEFAULT);
			stopUnfoldTimer();
		}
    }

	private boolean isInFoldingRegion(DropTargetDragEvent dtde) {
		final MainView node = getNode(dtde);
		return node.isInFoldingRegion(dtde.getLocation());
	}

	@Override
	public void dragExit(final DropTargetEvent e) {
		getNode(e).setMouseArea(MouseArea.OUT);
		stopUnfoldTimer();
		final MainView mainView = getNode(e);
		mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
		mainView.repaint();
	}

	private MainView getNode(final DropTargetEvent e) {
	    final Component draggedNode = e.getDropTargetContext().getComponent();
		final MainView mainView = (MainView) draggedNode;
	    return mainView;
    }

	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
		if(isDragAcceptable(dtde)) {
			supportFolding(dtde);

			final MainView draggedNode = (MainView) dtde.getDropTargetContext().getComponent();
			final int oldDraggedOver = draggedNode.getDraggedOver();
			draggedNode.setDraggedOver(dtde.getLocation());
			final int newDraggedOver = draggedNode.getDraggedOver();
			final boolean repaint = newDraggedOver != oldDraggedOver;
			if (repaint) {
				draggedNode.repaint();
			}
		}
	}

	private void startUnfoldTimer(final MainView mainView) {
		if(timer == null){
			timer = new Timer(UNFOLD_DELAY_MILLISECONDS, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(mainView.isDisplayable()){
						NodeView nodeView = mainView.getNodeView();
						final NodeModel node = nodeView.getModel();
						Controller.getCurrentModeController().getMapController().unfold(node, nodeView.getMap().getFilter());
					}
				}
			});
			timer.setRepeats(false);
			timer.start();
		}
    }

	private void stopUnfoldTimer() {
	    if(timer != null){
	    	timer.stop();
	    	timer = null;
	    }

    }

	public void dragScroll(final DropTargetDragEvent e) {
	}

	private boolean isDropAcceptable(final DropTargetDropEvent event, int dropAction) {
		if (! event.isDataFlavorSupported(MindMapNodesSelection.mindMapNodeObjectsFlavor))
			 return dropAction != DnDConstants.ACTION_LINK;
		final List<NodeModel> droppedNodes;
		try {
			final Transferable t = event.getTransferable();
			droppedNodes = getNodeObjects(t);
		}
		catch (Exception e) {
			return dropAction != DnDConstants.ACTION_LINK;
		}
		final NodeModel node = ((MainView) event.getDropTargetContext().getComponent()).getNodeView().getModel();
		if (dropAction == DnDConstants.ACTION_LINK) {
			return isFromSameMap(node, droppedNodes);
		}

		if (dropAction == DnDConstants.ACTION_MOVE) {
			return !isFromDescencantNode(node, droppedNodes);
		}
		return ! droppedNodesContainTargetNode(node, droppedNodes);
	}

	private boolean droppedNodesContainTargetNode(final NodeModel targetNode, final List<NodeModel> droppedNodes) {
		for (final NodeModel selected : droppedNodes) {
			if (targetNode == selected)
				return true;
		}
		return false;
	}

	private boolean isFromSameMap(final NodeModel targetNode, final Collection<NodeModel> droppedNodes) {
		for (final NodeModel selected : droppedNodes) {
			if (selected.getMap() != targetNode.getMap())
				return false;
		}
		return true;
	}

	private boolean isFromDescencantNode(final NodeModel targetNode, final List<NodeModel> droppedNodes) {
		for (final NodeModel selected : droppedNodes) {
			if ((targetNode == selected) || targetNode.isDescendantOf(selected))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private List<NodeModel> getNodeObjects(final Transferable t) throws UnsupportedFlavorException, IOException {
	    return (List<NodeModel>) t.getTransferData(MindMapNodesSelection.mindMapNodeObjectsFlavor);
    }

	@Override
	public void drop(final DropTargetDropEvent dtde) {
		try {
			final MainView mainView = (MainView) dtde.getDropTargetContext().getComponent();
			final NodeView targetNodeView = mainView.getNodeView();
			final MapView mapView = targetNodeView.getMap();
			mapView.select();
			final NodeModel targetNode = targetNodeView.getModel();
			final Controller controller = Controller.getCurrentController();
			int dropAction = getDropAction(dtde);
			final Transferable t = dtde.getTransferable();
			mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
			mainView.repaint();
			if (dtde.isLocalTransfer() && !isDropAcceptable(dtde, dropAction)) {
				dtde.rejectDrop();
				return;
			}
			final boolean dropAsSibling = mainView.dropAsSibling(dtde.getLocation().getX());
			ModeController modeController = controller.getModeController();
			final MMapController mapController = (MMapController) modeController.getMapController();
			if ((dropAction == DnDConstants.ACTION_MOVE || dropAction == DnDConstants.ACTION_COPY)) {
				final NodeModel parent = dropAsSibling ? targetNode.getParentNode() : targetNode;
				if (!mapController.isWriteable(parent)) {
					dtde.rejectDrop();
					final String message = TextUtils.getText("node_is_write_protected");
					UITools.errorMessage(message);
					return;
				}
			}
			final boolean isLeft = mainView.dropLeft(dtde.getLocation().getX());
			if (!dtde.isLocalTransfer()) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				((MMapClipboardController) MapClipboardController.getController()).paste(t, targetNode, dropAsSibling, isLeft, dropAction);
				dtde.dropComplete(true);
				return;
			}
			dtde.acceptDrop(dropAction);
			if (dropAction == DnDConstants.ACTION_LINK) {
				int yesorno = JOptionPane.YES_OPTION;
				if (controller.getSelection().size() >= 5) {
					yesorno = JOptionPane.showConfirmDialog(controller.getViewController().getCurrentRootComponent(), TextUtils
					    .getText("lots_of_links_warning"), Integer.toString(controller.getSelection().size())
					        + " links to the same node", JOptionPane.YES_NO_OPTION);
				}
				if (yesorno == JOptionPane.YES_OPTION) {
					for (final NodeModel sourceNodeModel : getNodeObjects(t)) {

						((MLinkController) LinkController.getController(modeController)).addConnector(
						    sourceNodeModel, targetNode);
					}
				}
			}
			else {
				final Collection<NodeModel> selecteds = mapController.getSelectedNodes();
				if (DnDConstants.ACTION_MOVE == dropAction && isFromSameMap(targetNode, selecteds)) {
	                final NodeModel[] array = selecteds.toArray(new NodeModel[selecteds.size()]);
					moveNodes(mapController, targetNode, t, dropAsSibling, isLeft);

					if(dropAsSibling || ! targetNodeView.isFolded())
					    controller.getSelection().replaceSelection(array);
					else
					    controller.getSelection().selectAsTheOnlyOneSelected(targetNode);
				}
				else if (DnDConstants.ACTION_COPY == dropAction || DnDConstants.ACTION_MOVE == dropAction) {
					((MMapClipboardController) MapClipboardController.getController()).paste(t, targetNode, dropAsSibling,
					    isLeft);
	                controller.getSelection().selectAsTheOnlyOneSelected(targetNode);
				}
			}
		}
		catch (final Exception e) {
			LogUtils.severe("Drop exception:", e);
			dtde.dropComplete(false);
			return;
		}
		dtde.dropComplete(true);
	}

	private int getDropAction(final DropTargetDropEvent dtde) throws UnsupportedFlavorException, IOException {
		int dropAction = dtde.getDropAction();
		final Transferable t = dtde.getTransferable();
		if (dtde.isLocalTransfer() && t.isDataFlavorSupported(MindMapNodesSelection.dropActionFlavor)) {
			final String sourceAction = (String) t.getTransferData(MindMapNodesSelection.dropActionFlavor);
			if (sourceAction.equals("LINK")) {
				dropAction = DnDConstants.ACTION_LINK;
			}
			if (sourceAction.equals("COPY")) {
				dropAction = DnDConstants.ACTION_COPY;
			}
		}
		return dropAction;
	}

	private void moveNodes(final MMapController mapController, final NodeModel targetNode, Transferable t,
	                       final boolean dropAsSibling, final boolean isLeft) throws UnsupportedFlavorException,
	        IOException {
		final List<NodeModel> movedNodes = getNodeObjects(t);
		if (dropAsSibling) {
			mapController.moveNodesBefore(movedNodes, targetNode, isLeft, true);
		}
		else {
			mapController.moveNodesAsChildren(movedNodes, targetNode, isLeft, true);
		}
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent e) {
	}

	private boolean isDragAcceptable(final DropTargetDragEvent ev) {
		return ev.isDataFlavorSupported(DataFlavor.stringFlavor)
				||ev.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)
				||ev.isDataFlavorSupported(DataFlavor.imageFlavor);
	}

}
