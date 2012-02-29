package org.freeplane.plugin.workspace.components.menu;

import java.awt.Point;

import javax.swing.JPopupMenu;

public class WorkspacePopupMenu extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Point invokerLocation;
	
	public WorkspacePopupMenu(String popupName) {
		super(popupName);
	}
	
	public WorkspacePopupMenu() {
		super();
	}
	
	public Point getInvokerLocation() {
		return invokerLocation;
	}
	public void setInvokerLocation(Point invokerLocation) {
		this.invokerLocation = invokerLocation;
	}
}
