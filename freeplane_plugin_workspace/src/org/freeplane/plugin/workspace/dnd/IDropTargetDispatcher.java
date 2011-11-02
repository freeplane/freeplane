/**
 * author: Marcel Genzmehr
 * 21.10.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.dnd.DropTargetDropEvent;

/**
 * 
 */
public interface IDropTargetDispatcher {
	
	/**
	 * @param event a drop event
	 * @return <code>true</code> if a target was found that accepted this drop, else <code>false</code>
	 */
	public boolean dispatchDropEvent(DropTargetDropEvent event);
}
