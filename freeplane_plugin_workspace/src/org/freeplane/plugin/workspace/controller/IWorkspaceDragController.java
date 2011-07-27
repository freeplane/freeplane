/**
 * author: Marcel Genzmehr
 * 27.07.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.awt.Point;

import javax.swing.JTree;

/**
 * 
 */
public interface IWorkspaceDragController {
	public boolean canPerformAction(JTree target, Object draggedNode, int action, Point location);

	public boolean executeDrop(JTree tree, Object draggedNode, Object newParentNode, int action);
}
