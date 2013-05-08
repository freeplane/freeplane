package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;

public class FolderLinkNode extends AFolderNode implements IWorkspaceNodeActionListener
																, IWorkspaceTransferableCreator
																, IFileSystemRepresentation
																, IMutableLinkNode {
	
	private static final long serialVersionUID = 1L;
	private static Icon FOLDER_OPEN_ICON = new ImageIcon(FolderLinkNode.class.getResource("/images/16x16/folder-orange_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(FolderLinkNode.class.getResource("/images/16x16/folder-orange.png"));
	
	private static WorkspacePopupMenu popupMenu = null;
	
	private URI folderPath;
	private boolean doMonitoring = false;
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
		File file = URIUtils.getAbsoluteFile(uri);
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
			if(enable != this.doMonitoring) {
				this.doMonitoring = enable;
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
			folder = URIUtils.getAbsoluteFile(getPath());
			if (folder.isDirectory()) {
				getModel().removeAllElements(this);
				WorkspaceController.getFileSystemMgr().scanFileSystem(this, folder);
				getModel().reload(this);				
			}
		}
		catch (Exception e) {
			LogUtils.severe(e);
		}		
	}
	
	protected AWorkspaceTreeNode clone(FolderLinkNode node) {		
		node.setPath(getPath());		
		return super.clone(node);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
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

	public WorkspaceTransferable getTransferable() {
		WorkspaceTransferable transferable = new WorkspaceTransferable();
		try {
			URI uri = URIUtils.getAbsoluteURI(getPath());
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
		return URIUtils.getAbsoluteFile(this.getPath());
	}
	
	public boolean changeName(String newName, boolean renameLink) {
		assert(newName != null);
		assert(newName.trim().length() > 0);
		
		if(renameLink) {
			File oldFile = URIUtils.getAbsoluteFile(getPath());
			try{
				if(oldFile == null) {
					throw new Exception("failed to resolve the file for"+getName());
				}
				File destFile = new File(oldFile.getParentFile(), newName);
				if(oldFile.exists() && oldFile.renameTo(destFile)) {					
					//this.setName(newName);
					try {
						getModel().changeNodeName(this, newName);
						return true;
					}
					catch(Exception ex) {
						destFile.renameTo(oldFile);
						return false;
					}
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
			//this.setName(newName);
			try {
				getModel().changeNodeName(this, newName);
				return true;
			}
			catch(Exception ex) {
				// do nth.
			}
		}
		return false;
	}
}
