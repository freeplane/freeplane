/**
 * author: Marcel Genzmehr
 * 10.08.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.WorkspaceController;

/**
 * 
 */
public class DefaultWorkspaceMouseHandler implements MouseListener {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void mouseClicked(MouseEvent e) {
		TreePath path = ((JTree) e.getSource()).getPathForLocation(e.getX(), e.getY());
		WorkspaceController.getCurrentWorkspaceController().getWorkspaceView().getTree().addSelectionPath(path);
		if (path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

			if (node.getUserObject() instanceof IWorkspaceNodeEventListener) {
				int eventType = 0;
				if (e.getButton() == MouseEvent.BUTTON1) {
					eventType += WorkspaceNodeEvent.MOUSE_LEFT;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					eventType += WorkspaceNodeEvent.MOUSE_RIGHT;
				}
				if (e.getClickCount() % 2 == 0) {
					eventType += WorkspaceNodeEvent.MOUSE_DBLCLICK;
				}
				else {
					eventType += WorkspaceNodeEvent.MOUSE_CLICK;
				}
				((IWorkspaceNodeEventListener) node.getUserObject()).handleEvent(new WorkspaceNodeEvent(e.getComponent(),
						eventType, e.getX(), e.getY()));
			}

		}
		else {
			if (e.getButton() == MouseEvent.BUTTON3) {
				WorkspaceController.getCurrentWorkspaceController().getPopups().showWorkspacePopup(e.getComponent(), e.getX(), e.getY());
				
			}
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {
	}
}
