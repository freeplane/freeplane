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
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.clipboard.MindMapNodesSelection;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.clipboard.MClipboardController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

public class MNodeDropListener implements DropTargetListener {
	final private ModeController modeController;

	public MNodeDropListener(final ModeController controller) {
		modeController = controller;
	}

	/**
	 * The method is called when the cursor carrying the dragged item enteres
	 * the area of the node. The name "dragEnter" seems to be confusing to me. I
	 * think the difference between dragAcceptable and dropAcceptable is that in
	 * dragAcceptable, you tell if the type of the thing being dragged is OK,
	 * where in dropAcceptable, you tell if your really willing to accept the
	 * item.
	 */
	public void dragEnter(final DropTargetDragEvent dtde) {
		if (isDragAcceptable(dtde)) {
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		}
		else {
			dtde.rejectDrag();
		}
	}

	public void dragExit(final DropTargetEvent e) {
		final Component draggedNode = e.getDropTargetContext().getComponent();
		((MainView) draggedNode).setDraggedOver(NodeView.DRAGGED_OVER_NO);
		draggedNode.repaint();
	}

	public void dragOver(final DropTargetDragEvent e) {
		final MainView draggedNode = (MainView) e.getDropTargetContext().getComponent();
		final int oldDraggedOver = draggedNode.getDraggedOver();
		draggedNode.setDraggedOver(e.getLocation());
		final int newDraggedOver = draggedNode.getDraggedOver();
		final boolean repaint = newDraggedOver != oldDraggedOver;
		if (repaint) {
			draggedNode.repaint();
		}
	}

	public void dragScroll(final DropTargetDragEvent e) {
	}

	public void drop(final DropTargetDropEvent dtde) {
		try {
			int dropAction = dtde.getDropAction();
			final Transferable t = dtde.getTransferable();
			final MainView mainView = (MainView) dtde.getDropTargetContext().getComponent();
			final NodeView targetNodeView = mainView.getNodeView();
			final NodeModel targetNode = targetNodeView.getModel();
			final Controller controller = modeController.getController();
			if (dtde.isLocalTransfer() && t.isDataFlavorSupported(MindMapNodesSelection.dropActionFlavor)) {
				final String sourceAction = (String) t.getTransferData(MindMapNodesSelection.dropActionFlavor);
				if (sourceAction.equals("LINK")) {
					dropAction = DnDConstants.ACTION_LINK;
				}
				if (sourceAction.equals("COPY")) {
					dropAction = DnDConstants.ACTION_COPY;
				}
			}
			mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
			mainView.repaint();
			if (dtde.isLocalTransfer() && (dropAction == DnDConstants.ACTION_MOVE) && !isDropAcceptable(dtde)) {
				dtde.rejectDrop();
				return;
			}
			final boolean dropAsSibling = mainView.dropAsSibling(dtde.getLocation().getX());
			final MMapController mapController = (MMapController) modeController.getMapController();
			if ((dropAction == DnDConstants.ACTION_MOVE || dropAction == DnDConstants.ACTION_COPY)){
				final NodeModel parent = dropAsSibling ? targetNode.getParentNode() : targetNode;
				if( !mapController.isWriteable(parent)) {
					dtde.rejectDrop();
					final String message = ResourceBundles.getText("node_is_write_protected");
					UITools.errorMessage(message);
					return;
				}
			}
			dtde.acceptDrop(dtde.getDropAction());
			final boolean isLeft = mainView.dropPosition(dtde.getLocation().getX());
			if (!dtde.isLocalTransfer()) {
				((MClipboardController) ClipboardController.getController(modeController)).paste(t, targetNode,
				    dropAsSibling, isLeft);
				dtde.dropComplete(true);
				return;
			}
			if (dropAction == DnDConstants.ACTION_LINK) {
				int yesorno = JOptionPane.YES_OPTION;
				if (controller.getSelection().size() >= 5) {
					yesorno = JOptionPane.showConfirmDialog(controller.getViewController().getContentPane(),
					    ResourceBundles.getText("lots_of_links_warning"), Integer.toString(controller.getSelection()
					        .size())
					            + " links to the same node", JOptionPane.YES_NO_OPTION);
				}
				if (yesorno == JOptionPane.YES_OPTION) {
					for (final Iterator<NodeModel> it = controller.getSelection().getSelection().iterator(); it
					    .hasNext();) {
						final NodeModel selectedNodeModel = (it.next());
						((MLinkController) LinkController.getController(modeController)).addConnector(
						    selectedNodeModel, targetNode);
					}
				}
			}
			else {
				Transferable trans = null;
				final List selecteds = mapController.getSelectedNodes();
				if (DnDConstants.ACTION_MOVE == dropAction) {
					NodeModel actualNode = targetNode;
					do {
						if (selecteds.contains(actualNode)) {
							final String message = ResourceBundles.getText("cannot_move_to_child");
							JOptionPane.showMessageDialog(controller.getViewController().getContentPane(), message,
							    "Freeplane", JOptionPane.WARNING_MESSAGE);
							dtde.dropComplete(true);
							return;
						}
						actualNode = (actualNode.isRoot()) ? null : actualNode.getParentNode();
					} while (actualNode != null);
					final List<NodeModel> sortedSelection = controller.getSelection().getSortedSelection(true);
					for (final NodeModel node : sortedSelection) {
						mapController.moveNode(node, targetNode, dropAsSibling, isLeft, isLeft != node.isLeft());
					}
				}
				else {
					trans = ClipboardController.getController(modeController).copy(controller.getSelection());
					((MClipboardController) ClipboardController.getController(modeController)).paste(trans, targetNode,
					    dropAsSibling, isLeft);
				}
				controller.getSelection().selectAsTheOnlyOneSelected(targetNode);
			}
		}
		catch (final Exception e) {
			LogTool.severe("Drop exception:", e);
			dtde.dropComplete(false);
			return;
		}
		dtde.dropComplete(true);
	}

	public void dropActionChanged(final DropTargetDragEvent e) {
	}

	private boolean isDragAcceptable(final DropTargetDragEvent ev) {
		if (ev.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return true;
		}
		if (ev.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)) {
			return true;
		}
		return false;
	}

	private boolean isDropAcceptable(final DropTargetDropEvent event) {
		final NodeModel node = ((MainView) event.getDropTargetContext().getComponent()).getNodeView().getModel();
		final NodeModel selected = modeController.getMapController().getSelectedNode();
		return ((node != selected) && !node.isDescendantOf(selected));
	}
}
