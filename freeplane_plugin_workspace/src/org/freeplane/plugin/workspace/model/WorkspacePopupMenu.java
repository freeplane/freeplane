package org.freeplane.plugin.workspace.model;

import java.awt.Point;

import javax.swing.JPopupMenu;

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
