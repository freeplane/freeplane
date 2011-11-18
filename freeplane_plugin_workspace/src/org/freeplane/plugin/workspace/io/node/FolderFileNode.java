/**
 * author: Marcel Genzmehr
 * 22.07.2011
 */
package org.freeplane.plugin.workspace.io.node;

import java.io.File;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderFileNode extends DefaultFileNode {
	private static final Icon FOLDER_OPEN_ICON = new ImageIcon(DefaultFileNode.class.getResource("/images/16x16/folder-orange_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(DefaultFileNode.class.getResource("/images/16x16/folder-orange.png"));
	
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
				WorkspaceUtils.getModel().removeAllElements(this);
				WorkspaceController.getController().getFilesystemMgr().scanFileSystem(this, getFile());
				WorkspaceUtils.getModel().reload(this);
				WorkspaceController.getController().getExpansionStateHandler().restoreExpansionStates();				
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
						"workspace.action.file.new.directory",
						"workspace.action.file.new.mindmap",
						//WorkspacePopupMenuBuilder.SEPARATOR,
						//"workspace.action.file.new.file",
						WorkspacePopupMenuBuilder.endSubMenu(),
						WorkspacePopupMenuBuilder.SEPARATOR, 
						"workspace.action.node.paste",
						"workspace.action.node.copy",
						"workspace.action.node.cut",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.rename",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.refresh",
						"workspace.action.node.delete"		
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
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void handleEvent(WorkspaceNodeEvent event) {	
		System.out.println("FolderFileNode: "+ event);
		if(event.getType() == WorkspaceNodeEvent.WSNODE_CHANGED) {
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
		else if(event.getType() == WorkspaceNodeEvent.WSNODE_OPEN_DOCUMENT) {
			//do nth
		}
		else {
			super.handleEvent(event);
		}
	}
	
	
}
