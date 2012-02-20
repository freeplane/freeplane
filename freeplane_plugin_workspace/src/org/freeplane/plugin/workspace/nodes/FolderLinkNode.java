package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;

public class FolderLinkNode extends AFolderNode implements IWorkspaceNodeActionListener
																, FileAlterationListener
																, IWorkspaceTransferableCreator
																, IDropAcceptor
																, IFileSystemRepresentation
																, IMutableLinkNode {
	
	private static final long serialVersionUID = 1L;
	private static Icon FOLDER_OPEN_ICON = new ImageIcon(FolderLinkNode.class.getResource("/images/16x16/folder-orange_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(FolderLinkNode.class.getResource("/images/16x16/folder-orange.png"));
	
	private static WorkspacePopupMenu popupMenu = null;
	
	private URI folderPath;
	private boolean doMonitoring = false;
	private boolean first;
	private boolean orderDescending = false;
	
	public FolderLinkNode() {
		this(AFolderNode.FOLDER_TYPE_PHYSICAL);
	}

	public FolderLinkNode(String id) {
		super(id);
	}

	@ExportAsAttribute(name="path")
	public URI getPath() {
		return folderPath;
	}

	public void setPath(URI uri) {
		if(isMonitoring()) {
			enableMonitoring(false);
			this.folderPath = uri;
			createIfNeeded(getPath());
			enableMonitoring(true);
		} 
		else {
			this.folderPath = uri;
			createIfNeeded(getPath());
		}		
	}
	
	private void createIfNeeded(URI uri) {
		File file = WorkspaceUtils.resolveURI(uri);
		if (file != null && !file.exists()) {
			file.mkdirs();			
		}
	}

	public void initializePopup() {
		if (popupMenu == null) {			
			
			if (popupMenu == null) {			
				popupMenu = new WorkspacePopupMenu();
				WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
						WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
						"workspace.action.node.new.folder",
						"workspace.action.file.new.mindmap",
						//WorkspacePopupMenuBuilder.SEPARATOR,
						//"workspace.action.file.new.file",
						WorkspacePopupMenuBuilder.endSubMenu(),
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.open.location",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.cut",
						"workspace.action.node.copy",						
						"workspace.action.node.paste",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.rename",
						"workspace.action.node.remove",
						"workspace.action.file.delete",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.physical.sort",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.enable.monitoring",
						"workspace.action.node.refresh"		
				});
			}
		}
	}
	
	public void enableMonitoring(boolean enable) {
		if(getPath() == null) {
			this.doMonitoring = enable;
		} 
		else {
			File file = WorkspaceUtils.resolveURI(getPath());
			if(enable != this.doMonitoring) {
				this.doMonitoring = enable;
				first = true;
				if(file == null) {
					return;
				}
				try {		
					if(enable) {					
						WorkspaceController.getController().getFileSystemAlterationMonitor().addFileSystemListener(file, this);
					}
					else {
						WorkspaceController.getController().getFileSystemAlterationMonitor().removeFileSystemListener(file, this);
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@ExportAsAttribute(name="monitor")
	public boolean isMonitoring() {
		return this.doMonitoring;
	}
	
	public void orderDescending(boolean enable) {
		this.orderDescending = enable;
	}
	
	@ExportAsAttribute(name="orderDescending")
	public boolean orderDescending() {
		return orderDescending;
	}

	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
			event.consume();
		}
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(FOLDER_OPEN_ICON);
		renderer.setClosedIcon(FOLDER_CLOSED_ICON);
		renderer.setLeafIcon(FOLDER_CLOSED_ICON);
		return true;
	}

	public String toString() {
		return this.getClass().getSimpleName() + "[id=" + this.getId() + ";name=" + this.getName() + ";path="
				+ this.getPath() + "]";
	}

	public void refresh() {
		File folder;
		try {
			folder = WorkspaceUtils.resolveURI(getPath());
			if (folder.isDirectory()) {
				WorkspaceUtils.getModel().removeAllElements(this);
				WorkspaceController.getController().getFilesystemMgr().scanFileSystem(this, folder);
				WorkspaceUtils.getModel().reload(this);
				WorkspaceController.getController().getExpansionStateHandler().restoreExpansionStates();				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	protected AWorkspaceTreeNode clone(FolderLinkNode node) {		
		node.setPath(getPath());		
		return super.clone(node);
	}
	
	private void processWorkspaceNodeDrop(List<AWorkspaceTreeNode> nodes, int dropAction) {
		try {	
			File targetDir = WorkspaceUtils.resolveURI(getPath());
			for(AWorkspaceTreeNode node : nodes) {
				if(node instanceof DefaultFileNode) {					
					if(targetDir != null && targetDir.isDirectory()) {
						if(dropAction == DnDConstants.ACTION_COPY) {
							((DefaultFileNode) node).copyTo(targetDir);
						} 
						else if(dropAction == DnDConstants.ACTION_MOVE) {
							((DefaultFileNode) node).moveTo(targetDir);
							AWorkspaceTreeNode parent = node.getParent();
							WorkspaceUtils.getModel().removeNodeFromParent(node);
							parent.refresh();
						}
					}
				}
				else if(node instanceof LinkTypeFileNode) {
					File srcFile = WorkspaceUtils.resolveURI(((LinkTypeFileNode) node).getLinkPath());
					if(targetDir != null && targetDir.isDirectory()) {
						FileUtils.copyFileToDirectory(srcFile, targetDir);
						if(dropAction == DnDConstants.ACTION_MOVE) {
							AWorkspaceTreeNode parent = node.getParent();
							WorkspaceUtils.getModel().removeNodeFromParent(node);
							parent.refresh();
						}
					}
				}
			}
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}	
	}
	
	private void processFileListDrop(List<File> files, int dropAction) {
		try {
			File targetDir = WorkspaceUtils.resolveURI(getPath());			
			for(File srcFile : files) {
				if(srcFile.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(srcFile, targetDir);
				}
				else {
					FileUtils.copyFileToDirectory(srcFile, targetDir, true);
				}				
			}
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
	}
	
	private void processUriListDrop(List<URI> uris, int dropAction) {
		try {
			File targetDir = WorkspaceUtils.resolveURI(getPath());			
			for(URI uri : uris) {
				File srcFile = new File(uri);
				if(srcFile == null || !srcFile.exists()) {
					continue;
				}
				if(srcFile.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(srcFile, targetDir);
				}
				else {
					FileUtils.copyFileToDirectory(srcFile, targetDir, true);
				}				
			}
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
		
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	private boolean fsChanges = false;
	public void onStart(FileAlterationObserver observer) {
		fsChanges = false;
		if(first ) return;
		// called when the observer starts a check cycle. do nth so far. 
	}

	public void onDirectoryCreate(File directory) {
		if(first) return;
		fsChanges = true;
	}

	public void onDirectoryChange(File directory) {
		if(first) return;
		fsChanges = true;
	}

	public void onDirectoryDelete(File directory) {
		if(first) return;
		fsChanges = true;
	}

	public void onFileCreate(File file) {
		if(first) return;
		fsChanges = true;
	}

	public void onFileChange(File file) {
		if(first) return;
		fsChanges = true;
	}

	public void onFileDelete(File file) {
		if(first) return;
		fsChanges = true;	
	}

	public void onStop(FileAlterationObserver observer) {
		if(!first && fsChanges) {
			fsChanges=false;
			refresh();
		}
		first = false;
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	public AWorkspaceTreeNode clone() {
		FolderLinkNode node = new FolderLinkNode(getType());
		return clone(node);
	}
	
	public boolean acceptDrop(DataFlavor[] flavors) {
		for(DataFlavor flavor : flavors) {
			if(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR.equals(flavor)
				|| WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR.equals(flavor)
				|| WorkspaceTransferable.WORKSPACE_NODE_FLAVOR.equals(flavor)
			) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean processDrop(Transferable transferable, int dropAction) {
		try {
			if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				processWorkspaceNodeDrop((List<AWorkspaceTreeNode>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR), dropAction);	
			}
			else if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				processFileListDrop((List<File>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR), dropAction);
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
						uriList.add(URI.create(singleUri));
					}
					catch (Exception e) {
						LogUtils.info("DOCEAR - "+ e.getMessage());
					}
				}
				processUriListDrop(uriList, dropAction);	
			}
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		return true;
	}
	
	public boolean processDrop(DropTargetDropEvent event) {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		Transferable transferable = event.getTransferable();
		if(processDrop(transferable, event.getDropAction())) {
			event.dropComplete(true);
			return true;
		}
		event.dropComplete(false);
		return false;
	}

	public Transferable getTransferable() {
		WorkspaceTransferable transferable = new WorkspaceTransferable();
		try {
			URI uri = WorkspaceUtils.absoluteURI(getPath());
			transferable.addData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR, uri.toString());
			List<File> fileList = new Vector<File>();
			fileList.add(new File(uri));
			transferable.addData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR, fileList);
			if(!this.isSystem()) {
				List<AWorkspaceTreeNode> objectList = new ArrayList<AWorkspaceTreeNode>();
				objectList.add(this);
				transferable.addData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR, objectList);
			}
			return transferable;
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}		
		return null;
	}

	public File getFile() {
		return WorkspaceUtils.resolveURI(this.getPath());
	}
	
	public boolean changeName(String newName, boolean renameLink) {
		assert(newName != null);
		assert(newName.trim().length() > 0);
		
		if(renameLink) {
			File oldFile = WorkspaceUtils.resolveURI(getPath());
			try{
				if(oldFile == null) {
					throw new Exception("failed to resolve the file for"+getName());
				}
				File destFile = new File(oldFile.getParentFile(), newName);
				if(oldFile.exists() && oldFile.renameTo(destFile)) {					
					this.setName(newName);
					return true;
				}
				else {
					LogUtils.warn("cannot rename "+oldFile.getName());
				}
			}
			catch (Exception e) {
				LogUtils.warn("cannot rename "+oldFile.getName(), e);
			}
		}
		else {
			this.setName(newName);
			return true;
		}
		return false;
	}
}
