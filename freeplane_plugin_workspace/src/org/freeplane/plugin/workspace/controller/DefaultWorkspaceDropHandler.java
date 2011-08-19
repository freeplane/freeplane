/**
 * author: Marcel Genzmehr
 * 10.08.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.util.Compat;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.dnd.IWorkspaceDragnDropController;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;

/**
 * 
 */
public class DefaultWorkspaceDropHandler implements IWorkspaceDragnDropController {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public boolean canPerformAction(DropTargetDropEvent event) {
		return true;
	}

	public boolean executeDrop(DropTargetDropEvent event) {
		try {
			Transferable transferable = event.getTransferable();
			for (DataFlavor f : transferable.getTransferDataFlavors()) {
				System.out.println("DOCEAR: "+f);
			}
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List<?> list = (List<?>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR);
				for (Object item : list) {
					if(item instanceof File) {
						File file = (File)item;
						final Point location = event.getLocation();
						DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) WorkspaceController.getController().getWorkspaceViewTree().getPathForLocation(location.x, location.y).getLastPathComponent();
						if(targetNode.getUserObject() instanceof DefaultFileNode) {
							do {
								targetNode = (DefaultMutableTreeNode) targetNode.getParent();
							} while(targetNode.getUserObject() instanceof DefaultFileNode);
						} 
						System.out.println("Drop Item ("+file.toString()+") on "+targetNode.getUserObject()+" at "+location);
						if(file.isDirectory()) {
							WorkspaceUtils.createPhysicalFolderNode(file, targetNode);
						}
						else {
							WorkspaceUtils.createLinkTypeFileNode(file, targetNode);
						}
						WorkspaceController.getController().reloadWorkspace();
					}					
				}				
				event.getDropTargetContext().dropComplete(true);
				return true;
			}
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				String list = (String) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR);
				System.out.println(list);
				if (!list.startsWith("file://")) {
					return false;
				}
				final URI uri = new URI(new URL(list).toString());
				final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
				final File file = Compat.urlToFile(url);
				final Point location = event.getLocation();
				DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) WorkspaceController.getController().getWorkspaceViewTree().getPathForLocation(location.x, location.y).getLastPathComponent();
				if(targetNode.getUserObject() instanceof DefaultFileNode) {
					do {
						targetNode = (DefaultMutableTreeNode) targetNode.getParent();
					} while(targetNode.getUserObject() instanceof DefaultFileNode);
				} 
				System.out.println("Drop Item ("+file.toString()+") on "+targetNode.getUserObject()+" at "+location);
				if(file.isDirectory()) {
					WorkspaceUtils.createPhysicalFolderNode(file, targetNode);
				}
				else {
					WorkspaceUtils.createLinkTypeFileNode(file, targetNode);
				}
				WorkspaceController.getController().reloadWorkspace();
				
				event.getDropTargetContext().dropComplete(true);
				return true;
			}
			else if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List<?> path = (List<?>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR);
				for (Object item : path) {
					System.out.println(item.toString());
				}
				event.getDropTargetContext().dropComplete(true);
				return true;
			}
			else if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FREEPLANE_NODE_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Object object = transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FREEPLANE_NODE_FLAVOR);
				System.out.println(object.toString());
				event.getDropTargetContext().dropComplete(true);
				return true;
			}
			else if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_SERIALIZED_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Object object = transferable.getTransferData(WorkspaceTransferable.WORKSPACE_SERIALIZED_FLAVOR);
				System.out.println(object.toString());
				event.getDropTargetContext().dropComplete(true);
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			return false;
		}
	}
}
