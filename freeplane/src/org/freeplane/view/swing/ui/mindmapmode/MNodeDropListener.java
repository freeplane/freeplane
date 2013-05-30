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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.MindMapNodesSelection;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

public class MNodeDropListener implements DropTargetListener {
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
			final Controller controller = Controller.getCurrentController();
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
				((MClipboardController) ClipboardController.getController()).paste(t, targetNode, dropAsSibling, isLeft, dropAction);
				dtde.dropComplete(true);
				return;
			}
			dtde.acceptDrop(dropAction);
			if (dropAction == DnDConstants.ACTION_LINK) {
				int yesorno = JOptionPane.YES_OPTION;
				if (controller.getSelection().size() >= 5) {
					yesorno = JOptionPane.showConfirmDialog(controller.getViewController().getContentPane(), TextUtils
					    .getText("lots_of_links_warning"), Integer.toString(controller.getSelection().size())
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
				final Collection<NodeModel> selecteds = mapController.getSelectedNodes();
				if (DnDConstants.ACTION_MOVE == dropAction) {
					NodeModel actualNode = targetNode;
					do {
						if (selecteds.contains(actualNode)) {
							final String message = TextUtils.getText("cannot_move_to_child");
							JOptionPane.showMessageDialog(controller.getViewController().getContentPane(), message,
							    "Freeplane", JOptionPane.WARNING_MESSAGE);
							dtde.dropComplete(true);
							return;
						}
						actualNode = (actualNode.isRoot()) ? null : actualNode.getParentNode();
					} while (actualNode != null);
	                final NodeModel[] array = selecteds.toArray(new NodeModel[selecteds.size()]);
					final List<NodeModel> sortedSelection = controller.getSelection().getSortedSelection(true);
					for (final NodeModel node : sortedSelection) {
						boolean changeSide = isLeft != node.isLeft();
                        if (dropAsSibling) {
                        	mapController.moveNodeBefore(node, targetNode, isLeft, changeSide);
                        }
                        else {
                        	mapController.moveNodeAsChild(node, targetNode, isLeft, changeSide);
                        }
					}
					if(dropAsSibling || ! targetNode.isFolded())
					    controller.getSelection().replaceSelection(array);
					else
					    controller.getSelection().selectAsTheOnlyOneSelected(targetNode);
				}
				else {
					trans = ClipboardController.getController().copy(controller.getSelection());
					((MClipboardController) ClipboardController.getController()).paste(trans, targetNode, dropAsSibling, isLeft);
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
		final ModeController modeController = Controller.getCurrentController().getModeController();
		final NodeModel selected = modeController.getMapController().getSelectedNode();
		return ((node != selected) && !node.isDescendantOf(selected));
	}
}
