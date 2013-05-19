package org.freeplane.plugin.workspace.mindmapmode;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.FileSystemManager;
import org.freeplane.plugin.workspace.io.ITask;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.io.SkipTaskException;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;
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
			if (targetDir != null && targetDir.isDirectory()) {
				List<ITask> opList = new ArrayList<ITask>();
				for (AWorkspaceTreeNode node : nodes) {
					if (node instanceof DefaultFileNode) {
						File srcFile = ((DefaultFileNode) node).getFile();
						if (srcFile.equals(targetDir)) {
							continue;
						}
						File destFile = new File(targetDir, srcFile.getName());						
						if (dropAction == DnDConstants.ACTION_COPY) {
							FileSystemManager.buildCopyOperationList(srcFile, destFile, opList);
						} else if (dropAction == DnDConstants.ACTION_MOVE) {
							FileSystemManager.buildMoveOperationList(srcFile, destFile, opList);
							opList.add(getPostOperation(targetNode, node, srcFile, destFile));
						}
					} 
					else if (node instanceof LinkTypeFileNode) {
						File srcFile = URIUtils.getAbsoluteFile(((LinkTypeFileNode) node).getLinkURI());
						if (srcFile.equals(targetDir)) {
							continue;
						}
						File destFile = new File(targetDir, srcFile.getName());						

						FileSystemManager.buildCopyOperationList(srcFile, destFile, opList);
						if (dropAction == DnDConstants.ACTION_MOVE) {
							opList.add(getPostOperation(targetNode, node, srcFile, destFile));
						}
					} 
					else if (node instanceof FolderLinkNode) {
						File srcFile = URIUtils.getAbsoluteFile(((FolderLinkNode) node).getPath());
						if (srcFile.equals(targetDir)) {
							continue;
						}
						File destFile = new File(targetDir, srcFile.getName());						

						FileSystemManager.buildCopyOperationList(srcFile, destFile, opList);
						if (dropAction == DnDConstants.ACTION_MOVE) {
							opList.add(getPostOperation(targetNode, node, srcFile, destFile));
						}
					}
				}
				FileSystemManager.execOperations(opList);
			}			
		} catch (Exception e) {
			LogUtils.warn(e);
		}
	}

	private ITask getPostOperation(final AWorkspaceTreeNode targetNode, final AWorkspaceTreeNode node, final File srcFile, final File destFile) {
		return new ITask() {			
			public void exec(Properties properties) throws IOException {
				if(onSkipList(destFile.getParentFile(), properties)) {
					throw new SkipTaskException();
				}
				AWorkspaceTreeNode parent = node.getParent();
				targetNode.getModel().cutNodeFromParent(node);
				parent.refresh();
				targetNode.getModel().nodeMoved(node, srcFile, destFile);
			}
			
			private boolean onSkipList(File dest, Properties properties) {
				if(properties == null || dest == null) {
					return false;
				}
				String list = properties.getProperty("skippedDirs", "");
				String entry = dest.getPath()+";";
				if(list.contains(entry)) {
					return true;
				}
				return false;
			}
		};
		
	}

	private void processFileListDrop(AWorkspaceTreeNode targetNode, List<File> files, int dropAction) {
		try {
			File targetDir = ((IFileSystemRepresentation) targetNode).getFile();
			FileSystemManager.copyFiles(files, targetDir, false);
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
			
			IWorkspaceView view = WorkspaceController.getCurrentModeExtension().getView();
			if(view != null) {
				view.expandPath(targetNode.getTreePath());
				WorkspaceController.getCurrentModeExtension().getView().refreshView();
			}
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