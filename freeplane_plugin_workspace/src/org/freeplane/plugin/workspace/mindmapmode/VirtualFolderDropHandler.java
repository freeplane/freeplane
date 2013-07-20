package org.freeplane.plugin.workspace.mindmapmode;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.dnd.INodeDropHandler;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class VirtualFolderDropHandler implements INodeDropHandler {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private void processWorkspaceNodeDrop(AWorkspaceTreeNode targetNode, List<AWorkspaceTreeNode> nodes, int dropAction) {
		try {	
			for(AWorkspaceTreeNode node : nodes) {
				AWorkspaceTreeNode newNode = null;
				if(node instanceof DefaultFileNode) {					
					newNode = createFSNodeLinks(targetNode, ((DefaultFileNode) node).getFile());
				}
				else {
					if(dropAction == DnDConstants.ACTION_COPY) {
						newNode = node.clone();
					} 
					else if (dropAction == DnDConstants.ACTION_MOVE) {
						AWorkspaceTreeNode parent = node.getParent();
						targetNode.getModel().cutNodeFromParent(node);
						parent.refresh();
						newNode = node;
					}
				}
				if(newNode == null) {
					continue;
				}
				targetNode.getModel().addNodeTo(newNode, targetNode);
//				WorkspaceController.getController().getExpansionStateHandler().addPathKey(this.getKey());
			}
//			WorkspaceUtils.saveCurrentConfiguration();
			
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
	}
	
	private void processFileListDrop(AWorkspaceTreeNode targetNode, List<File> files, int dropAction) {
		try {		
			for(File srcFile : files) {
				AWorkspaceTreeNode node = createFSNodeLinks(targetNode, srcFile);
				targetNode.getModel().addNodeTo(node, targetNode);
				node.refresh();
			}
//			WorkspaceUtils.saveCurrentConfiguration();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
	}
	
	private void processUriListDrop(AWorkspaceTreeNode targetNode, List<URI> uris, int dropAction) {
		try {			
			for(URI uri : uris) {
				File srcFile = new File(uri);
				if(srcFile == null || !srcFile.exists()) {
					continue;
				}
				AWorkspaceTreeNode node = createFSNodeLinks(targetNode, srcFile);
				targetNode.getModel().addNodeTo(node, targetNode);
				node.refresh();
			};
//			WorkspaceUtils.saveCurrentConfiguration();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}		
	}	
	
	/**
	 * @param file
	 * @return
	 */
	private AWorkspaceTreeNode createFSNodeLinks(AWorkspaceTreeNode targetNode, File file) {
		AWorkspaceTreeNode node = null;
		AWorkspaceProject project = WorkspaceController.getProject(targetNode);
		if(file.isDirectory()) {
			FolderLinkNode pNode = new FolderLinkNode();			
			pNode.setPath(project.getRelativeURI(file.toURI()));
			node = pNode;
		}
		else {
			LinkTypeFileNode lNode = new LinkTypeFileNode();
			lNode.setLinkURI(project.getRelativeURI(file.toURI()));
			node = lNode;
		}
		node.setName(file.getName());
		return node;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@SuppressWarnings("unchecked")
	public boolean processDrop(AWorkspaceTreeNode targetNode, Transferable transferable, int dropAction) {
		try {
			if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				processWorkspaceNodeDrop(targetNode, (List<AWorkspaceTreeNode>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR), dropAction);	
			}
			else if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				processFileListDrop(targetNode, (List<File>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR), dropAction);
			} 
			else if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR)) {
				ArrayList<URI> uriList = new ArrayList<URI>();
				String uriString = (String) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR);
				if (!uriString.startsWith("file://")) {
					return false;
				}
				String[] uriArray = uriString.split("\r\n");
				for(String singleUri : uriArray) {
					try {
						uriList.add(URIUtils.createURI(singleUri));
					}
					catch (Exception e) {
						LogUtils.info("org.freeplane.plugin.workspace.mindmapmode.VirtualFolderDropHandler.processDrop(targetNode, transferable, dropAction)@1:"+ e.getMessage());
					}
				}
				processUriListDrop(targetNode, uriList, dropAction);	
			}
			targetNode.refresh();
			
			IWorkspaceView view = WorkspaceController.getCurrentModeExtension().getView();
			if(view != null) {
				view.expandPath(targetNode.getTreePath());
				WorkspaceController.getCurrentModeExtension().getView().refreshView();
			}
		}
		catch (Exception e) {
			LogUtils.warn("org.freeplane.plugin.workspace.mindmapmode.VirtualFolderDropHandler.processDrop(targetNode, transferable, dropAction)@2", e);
		}
		return true;
	}	

	public boolean acceptDrop(Transferable transferable) {
		for(DataFlavor flavor : transferable.getTransferDataFlavors()) {
			if(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR.equals(flavor)
				|| WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR.equals(flavor)
				|| WorkspaceTransferable.WORKSPACE_NODE_FLAVOR.equals(flavor)
			) {
				return true;
			}
		}
		return false;
	}
}
