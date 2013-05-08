/**
 * author: Marcel Genzmehr
 * 22.07.2011
 */
package org.freeplane.plugin.workspace.nodes;

import java.io.File;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderFileNode extends DefaultFileNode {
	private static final Icon FOLDER_OPEN_ICON = new ImageIcon(DefaultFileNode.class.getResource("/images/16x16/folder-orange_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(DefaultFileNode.class.getResource("/images/16x16/folder-orange.png"));
	private static final Icon NOT_EXISTING = new ImageIcon(DefaultFileNode.class.getResource("/images/16x16/folder-orange-missing.png"));
	
	private static final long serialVersionUID = 1L;
	
	private static WorkspacePopupMenu popupMenu = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	/**
	 * @param name
	 * @param file
	 */
	public FolderFileNode(String name, File file) {
		super(name, file);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void delete() {
		delete(getFile());
	}
	
	private void delete(File file) {
		if(file.isDirectory()) {
			for(File child : file.listFiles()) {
				delete(child);
			}			
		}
		file.delete();	
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		if(getFile() == null || !getFile().exists()) {
			renderer.setLeafIcon(NOT_EXISTING);
			renderer.setOpenIcon(NOT_EXISTING);
			renderer.setClosedIcon(NOT_EXISTING);
			return true;
		}
		renderer.setOpenIcon(FOLDER_OPEN_ICON);
		renderer.setClosedIcon(FOLDER_CLOSED_ICON);
		renderer.setLeafIcon(FOLDER_CLOSED_ICON);
		return true;
	}
	
	public AWorkspaceTreeNode clone() {
		FolderFileNode node = new FolderFileNode(getName(), getFile());
		return clone(node);
	}
	
	public void refresh() {
		try {
			if (getFile().isDirectory()) {
				getModel().removeAllElements(this);
				WorkspaceController.getFileSystemMgr().scanFileSystem(this, getFile());
				getModel().reload(this);				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
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
						"workspace.action.file.delete",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.refresh"		
				});
			}
		}
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
		
	public boolean isLeaf() {
		return false;
	}
		
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void handleAction(WorkspaceActionEvent event) {	
		if(event.getType() == WorkspaceActionEvent.WSNODE_CHANGED) {
			if(rename(event.getBaggage().toString())) {
				setName(event.getBaggage().toString());
				if(event.getSource() instanceof AWorkspaceTreeNode) {
					Enumeration<AWorkspaceTreeNode> childs = ((AWorkspaceTreeNode)event.getSource()).children();
					while(childs.hasMoreElements()) {
						AWorkspaceTreeNode node = ((AWorkspaceTreeNode) childs.nextElement());
						if(node instanceof DefaultFileNode) {
							((DefaultFileNode)node).relocateFile(getFile());							
						}
					}
				}
			}
			else {
				LogUtils.warn("Could not rename File("+getName()+") to File("+event.getBaggage()+")");
			}
			
		} 
		else if(event.getType() == WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT) {
//			try {
//				Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(getFile()));
//				event.consume();
//			} catch (Exception e) {
//				LogUtils.warn(e);
//			}
		}
		else {
			super.handleAction(event);
		}
	}
	
	public boolean getAllowsChildren() {
		return true;
	}
}
