/**
 * author: Marcel Genzmehr
 * 10.08.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.view.WorkspaceNodeRenderer;

/**
 * 
 */
public class DefaultWorkspaceMouseHandler implements MouseListener, MouseMotionListener {

	private TreePath lastSelection = null;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public final TreePath getLastSelectionPath() {
		return lastSelection;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void mouseClicked(MouseEvent e) {
		TreePath path = ((JTree) e.getSource()).getPathForLocation(e.getX(), e.getY());
		
		WorkspaceController.getController().getWorkspaceViewTree().addSelectionPath(path);
		if (path != null) {
			
			AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();

			if (node instanceof IWorkspaceNodeEventListener) {
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
				((IWorkspaceNodeEventListener) node).handleEvent(new WorkspaceNodeEvent(node,
						eventType, e.getX(), e.getY(), e.getComponent()));
			}

		}
		else {
			if (e.getButton() == MouseEvent.BUTTON3) {
				//WorkspaceController.getController().getPopups().showWorkspacePopup(e.getComponent(), e.getX(), e.getY());
				((AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot()).showPopup(e.getComponent(), e.getX(), e.getY());
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

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		JTree tree = ((JTree) e.getSource());		
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		if(path == getLastSelectionPath()) {
			return;
		}
		WorkspaceNodeRenderer renderer = (WorkspaceNodeRenderer) tree.getCellRenderer();
		if(path != null && path != getLastSelectionPath()) {								
			lastSelection = path;			
			renderer.highlightRow(tree.getRowForLocation(e.getX(), e.getY()));
			tree.repaint();
		} 
		else if(getLastSelectionPath() != null) {			
			lastSelection = null;
			renderer.highlightRow(-1);
			tree.repaint();
		}		
	}
}
