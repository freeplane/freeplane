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
package org.freeplane.view.swing.features.filepreview;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;

import javax.swing.SwingUtilities;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.clipboard.MindMapNodesSelection;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.NodeView;

public class MExternalImageDropListener implements DropTargetListener {
// 	final private ModeController modeController;

	public MExternalImageDropListener() {
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
	}

	public void dragOver(final DropTargetDragEvent e) {
	}

	public void dragScroll(final DropTargetDragEvent e) {
	}

	public void drop(final DropTargetDropEvent ev) {
		try {
			int dropAction = ev.getDropAction();
			if (dropAction == DnDConstants.ACTION_MOVE && ev.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)) {
				try {
					ev.acceptDrop(ev.getDropAction());
		            @SuppressWarnings("unchecked")
                    final List<File> transferData = (List<File>) ev.getTransferable().getTransferData(MindMapNodesSelection.fileListFlavor);
		            if(transferData.size() != 1)
		            	return;
		            final Component target = ev.getDropTargetContext().getComponent();
		            NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, target);
		            final File file = transferData.get(0);
		    		final ViewerController vc = (Controller.getCurrentController().getModeController()
		    			    .getExtension(ViewerController.class));
		    		final NodeModel node = nodeView.getModel();
					vc.paste(file.toURI(), node, node.isLeft());
	            }
	            catch (Exception e) {
	            	LogUtils.warn(e);
	            }
			}
		}
		catch (final Exception e) {
			LogUtils.severe("Drop exception:", e);
			ev.dropComplete(false);
			return;
		}
		ev.dropComplete(true);
	}

	public void dropActionChanged(final DropTargetDragEvent e) {
	}

	private boolean isDragAcceptable(final DropTargetDragEvent ev) {
		if (ev.getDropAction() == DnDConstants.ACTION_MOVE && ev.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)) {
			try {
				@SuppressWarnings("unchecked")
	            final List<File> transferData = (List<File>) ev.getTransferable().getTransferData(MindMapNodesSelection.fileListFlavor);
	            if(transferData.size() != 1)
	            	return false;
	            final File file = transferData.get(0);
	            if(! file.canRead())
	            	return false;
	            return true;
            }
            catch (Exception e) {
            }
		}
		return false;
	}
}
