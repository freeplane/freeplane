package org.freeplane.plugin.workspace.view;

import java.awt.Point;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

public class WorkspacePopupMenu extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Point invokerLocation;
	
	public Point getInvokerLocation() {
		return invokerLocation;
	}
	public void setInvokerLocation(Point invokerLocation) {
		this.invokerLocation = invokerLocation;
	}
}
