/**
 * author: Marcel Genzmehr
 * 22.07.2011
 */
package org.freeplane.plugin.workspace.io.node;

import java.io.File;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

/**
 * 
 */
public class FolderFileNode extends DefaultFileNode {

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
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public void handleEvent(WorkspaceNodeEvent event) {	
		System.out.println("FolderFileNode: "+ event);
		if(event.getType() == WorkspaceNodeEvent.WSNODE_CHANGED) {
			if(rename(event.getBaggage().toString())) {
				setName(event.getBaggage().toString());
				if(event.getSource() instanceof DefaultMutableTreeNode) {
					Enumeration<?> childs = ((DefaultMutableTreeNode)event.getSource()).children();
					while(childs.hasMoreElements()) {
						DefaultMutableTreeNode node = ((DefaultMutableTreeNode) childs.nextElement());
						if(node.getUserObject() instanceof DefaultFileNode) {
							((DefaultFileNode)node.getUserObject()).relocateFile(getFile());
							
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
