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
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderFileNode extends DefaultFileNode {
	private static final Icon FOLDER_OPEN_ICON = new ImageIcon(DefaultFileNode.class.getResource("/images/16x16/folder-orange_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(DefaultFileNode.class.getResource("/images/16x16/folder-orange.png"));
	
	
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
			
		} else {
			super.handleEvent(event);
		}
	}
}
