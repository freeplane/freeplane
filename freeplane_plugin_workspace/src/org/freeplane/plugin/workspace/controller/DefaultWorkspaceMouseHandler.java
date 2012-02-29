/**
 * author: Marcel Genzmehr
 * 10.08.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.WorkspaceNodeRenderer;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

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
			// encode buttons
			int eventType = 0;
			if (e.getButton() == MouseEvent.BUTTON1) {
				eventType += WorkspaceActionEvent.MOUSE_LEFT;
			}
			if (e.getButton() == MouseEvent.BUTTON3) {
				eventType += WorkspaceActionEvent.MOUSE_RIGHT;
			}
			if (e.getClickCount() % 2 == 0) {
				eventType += WorkspaceActionEvent.MOUSE_DBLCLICK;
			}
			else {
				eventType += WorkspaceActionEvent.MOUSE_CLICK;
			}
			
			WorkspaceActionEvent event = new WorkspaceActionEvent(node, eventType, e.getX(), e.getY(), e.getComponent());
			
			List<IWorkspaceNodeActionListener> nodeEventListeners = WorkspaceController.getIOController().getNodeActionListeners(node.getClass(), eventType);
			if(nodeEventListeners != null)  {
				for(IWorkspaceNodeActionListener listener : nodeEventListeners) {
					if(event.isConsumed()) {
						break;
					}
					listener.handleAction(event);
				}
			}
			
			if (!event.isConsumed() && node instanceof IWorkspaceNodeActionListener) {				
				((IWorkspaceNodeActionListener) node).handleAction(event);
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

	public void mouseDragged(MouseEvent e) {		
	}

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
