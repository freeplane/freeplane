/**
 * author: Marcel Genzmehr
 * 10.08.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * 
 */
public class DefaultWorkspaceKeyHandler implements KeyListener {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)((JTree)e.getSource()).getSelectionPath().getLastPathComponent();
			if (node.getUserObject() instanceof IWorkspaceNodeEventListener) {
				((IWorkspaceNodeEventListener) node.getUserObject()).handleEvent(new WorkspaceNodeEvent(e.getComponent(), WorkspaceNodeEvent.WSNODE_OPEN_DOCUMENT, 0, 0));
				e.consume();
			}
		}
	}

	public void keyReleased(KeyEvent e) {
	}
}
