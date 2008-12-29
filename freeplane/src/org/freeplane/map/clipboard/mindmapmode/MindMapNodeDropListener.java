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
package org.freeplane.map.clipboard.mindmapmode;

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

import org.freeplane.controller.Controller;
import org.freeplane.map.clipboard.MindMapNodesSelection;
import org.freeplane.map.link.mindmapmode.MLinkController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MMapController;
import org.freeplane.map.tree.view.MainView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.mindmapmode.MModeController;

public class MindMapNodeDropListener implements DropTargetListener {
	final private MModeController mMindMapController;

	public MindMapNodeDropListener(final MModeController controller) {
		mMindMapController = controller;
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
		final MainView draggedNode = (MainView) e.getDropTargetContext().getComponent();
		draggedNode.setDraggedOver(NodeView.DRAGGED_OVER_NO);
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
			final NodeModel targetNodeModel = targetNode;
			if (dtde.isLocalTransfer()
			        && t.isDataFlavorSupported(MindMapNodesSelection.dropActionFlavor)) {
				final String sourceAction = (String) t
				    .getTransferData(MindMapNodesSelection.dropActionFlavor);
				if (sourceAction.equals("LINK")) {
					dropAction = DnDConstants.ACTION_LINK;
				}
				if (sourceAction.equals("COPY")) {
					dropAction = DnDConstants.ACTION_COPY;
				}
			}
			mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
			mainView.repaint();
			if (dtde.isLocalTransfer() && (dropAction == DnDConstants.ACTION_MOVE)
			        && !isDropAcceptable(dtde)) {
				dtde.rejectDrop();
				return;
			}
			dtde.acceptDrop(dtde.getDropAction());
			if (!dtde.isLocalTransfer()) {
				((MClipboardController) mMindMapController.getClipboardController()).paste(t,
				    targetNode, mainView.dropAsSibling(dtde.getLocation().getX()), mainView
				        .dropPosition(dtde.getLocation().getX()));
				dtde.dropComplete(true);
				return;
			}
			if (dropAction == DnDConstants.ACTION_LINK) {
				int yesorno = JOptionPane.YES_OPTION;
				if (mMindMapController.getMapView().getSelection().size() >= 5) {
					yesorno = JOptionPane.showConfirmDialog(Controller.getController()
					    .getViewController().getContentPane(), mMindMapController
					    .getText("lots_of_links_warning"), Integer.toString(mMindMapController
					    .getMapView().getSelection().size())
					        + " links to the same node", JOptionPane.YES_NO_OPTION);
				}
				if (yesorno == JOptionPane.YES_OPTION) {
					for (final Iterator<NodeView> it = mMindMapController.getMapView()
					    .getSelection().iterator(); it.hasNext();) {
						final NodeModel selectedNodeModel = (it.next()).getModel();
						((MLinkController) mMindMapController.getLinkController()).addLink(
						    selectedNodeModel, targetNodeModel);
					}
				}
			}
			else {
				if (!((MMapController) mMindMapController.getMapController())
				    .isWriteable(targetNode)) {
					final String message = mMindMapController.getText("node_is_write_protected");
					JOptionPane.showMessageDialog(Controller.getController().getViewController()
					    .getContentPane(), message, "Freemind", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Transferable trans = null;
				final List selecteds = mMindMapController.getSelectedNodes();
				if (DnDConstants.ACTION_MOVE == dropAction) {
					NodeModel actualNode = targetNode;
					do {
						if (selecteds.contains(actualNode)) {
							final String message = mMindMapController
							    .getText("cannot_move_to_child");
							JOptionPane.showMessageDialog(Controller.getController()
							    .getViewController().getContentPane(), message, "Freemind",
							    JOptionPane.WARNING_MESSAGE);
							dtde.dropComplete(true);
							return;
						}
						actualNode = (actualNode.isRoot()) ? null : actualNode.getParentNode();
					} while (actualNode != null);
					trans = ((MClipboardController) mMindMapController.getClipboardController())
					    .cut(mMindMapController.getMapView().getSelectedNodesSortedByY());
				}
				else {
					trans = mMindMapController.getClipboardController().copy(
					    mMindMapController.getMapView());
				}
				mMindMapController.getMapView().selectAsTheOnlyOneSelected(targetNodeView);
				((MClipboardController) mMindMapController.getClipboardController()).paste(trans,
				    targetNode, mainView.dropAsSibling(dtde.getLocation().getX()), mainView
				        .dropPosition(dtde.getLocation().getX()));
			}
		}
		catch (final Exception e) {
			System.err.println("Drop exception:" + e);
			org.freeplane.Tools.logException(e);
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
		final NodeModel node = ((MainView) event.getDropTargetContext().getComponent())
		    .getNodeView().getModel();
		final NodeModel selected = mMindMapController.getSelectedNode();
		if (!((MMapController) mMindMapController.getMapController()).isWriteable(node)) {
			return false;
		}
		return ((node != selected) && !node.isDescendantOf(selected));
	}
}
