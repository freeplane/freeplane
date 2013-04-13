package org.freeplane.plugin.workspace.mindmapmode;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class FileFolderDropHandler extends DefaultFileDropHandler {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	private void processWorkspaceNodeDrop(AWorkspaceTreeNode targetNode, List<AWorkspaceTreeNode> nodes, int dropAction) {
		try {
			File targetDir = ((IFileSystemRepresentation) targetNode).getFile();
			for (AWorkspaceTreeNode node : nodes) {
				if (node instanceof DefaultFileNode) {
					if (targetDir != null && targetDir.isDirectory()) {
						if (dropAction == DnDConstants.ACTION_COPY) {
							((DefaultFileNode) node).copyTo(targetDir);
						} else if (dropAction == DnDConstants.ACTION_MOVE) {
							File oldFile = ((DefaultFileNode) node).getFile();
							if (oldFile.equals(targetDir))
								return;
							((DefaultFileNode) node).moveTo(targetDir);
							File newFile = new File(targetDir, ((DefaultFileNode) node).getName());
							AWorkspaceTreeNode parent = node.getParent();
							targetNode.getModel().cutNodeFromParent(node);
							parent.refresh();
							targetNode.getModel().nodeMoved(node, oldFile, newFile);
						}
					}
				} else if (node instanceof LinkTypeFileNode) {
					File srcFile = URIUtils.getAbsoluteFile(((LinkTypeFileNode) node).getLinkURI());
					if (targetDir != null && targetDir.isDirectory()) {
						FileUtils.copyFileToDirectory(srcFile, targetDir);
						if (dropAction == DnDConstants.ACTION_MOVE) {
							AWorkspaceTreeNode parent = node.getParent();
							targetNode.getModel().cutNodeFromParent(node);
							parent.refresh();
							targetNode.getModel().nodeMoved(node, srcFile, new File(targetDir, srcFile.getName()));
						}
					}
				}
			}
		} catch (Exception e) {
			LogUtils.warn(e);
		}
	}

	private void processFileListDrop(AWorkspaceTreeNode targetNode, List<File> files, int dropAction) {
		try {
			File targetDir = ((IFileSystemRepresentation) targetNode).getFile();
			for (File srcFile : files) {
				if (srcFile.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(srcFile, targetDir);
				} else {
					FileUtils.copyFileToDirectory(srcFile, targetDir, true);
				}
			}
		} catch (Exception e) {
			LogUtils.warn(e);
		}
		targetNode.refresh();
	}

	private void processUriListDrop(AWorkspaceTreeNode targetNode, List<URI> uris, int dropAction) {
		try {
			File targetDir = ((IFileSystemRepresentation) targetNode).getFile();
			for (URI uri : uris) {
				File srcFile = new File(uri);
				if (srcFile == null || !srcFile.exists()) {
					continue;
				}
				if (srcFile.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(srcFile, targetDir);
				} else {
					FileUtils.copyFileToDirectory(srcFile, targetDir, true);
				}
			}
		} catch (Exception e) {
			LogUtils.warn(e);
		}
		targetNode.refresh();
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	@SuppressWarnings("unchecked")
	public boolean processDrop(AWorkspaceTreeNode targetNode, Transferable transferable, int dropAction) {
		try {
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				processWorkspaceNodeDrop(targetNode, (List<AWorkspaceTreeNode>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR),
						dropAction);
			} else if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				processFileListDrop(targetNode, (List<File>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR), dropAction);
			} else if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR)) {
				ArrayList<URI> uriList = new ArrayList<URI>();
				String uriString = (String) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR);
				if (!uriString.startsWith("file://")) {
					return false;
				}
				String[] uriArray = uriString.split("\r\n");
				for (String singleUri : uriArray) {
					try {
						uriList.add(URIUtils.createURI(singleUri));
					} catch (Exception e) {
						LogUtils.info("org.freeplane.plugin.workspace.mindmapmode.FolderFileDropHandler.processDrop(targetNode, transferable, dropAction)@1"
								+ e.getMessage());
					}
				}
				processUriListDrop(targetNode, uriList, dropAction);
			}

			targetNode.refresh();
			// WORKSPACE - todo: do sth to save the expanded state
			// if(WorkspaceController.getCurrentModeExtension().getView() instanceof IExpansionStateHandler) {
			// ((IExpansionStateHandler) WorkspaceController.getCurrentModeExtension().getView()).addPathKey(this.getKey());
			// }
		} catch (Exception e) {
			LogUtils.warn("org.freeplane.plugin.workspace.mindmapmode.FolderFileDropHandler.processDrop(targetNode, transferable, dropAction)@2", e);
		}

		return true;
	}

	public boolean acceptDrop(Transferable transferable) {
		for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
			if (WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR.equals(flavor) || WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR.equals(flavor)
					|| WorkspaceTransferable.WORKSPACE_NODE_FLAVOR.equals(flavor)) {
				return true;
			}
		}
		return false;
	}
}