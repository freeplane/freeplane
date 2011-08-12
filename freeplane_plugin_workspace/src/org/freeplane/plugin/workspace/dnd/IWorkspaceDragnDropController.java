/**
 * author: Marcel Genzmehr
 * 27.07.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.dnd.DropTargetDropEvent;

/**
 * 
 */
public interface IWorkspaceDragnDropController {
	public boolean canPerformAction(DropTargetDropEvent event);

	public boolean executeDrop(DropTargetDropEvent event);
}
