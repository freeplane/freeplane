/**
 * author: Marcel Genzmehr
 * 21.10.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDropEvent;

/**
 * 
 */
public interface IDropAcceptor {
	public boolean acceptDrop(DataFlavor[] flavors);
	public boolean processDrop(DropTargetDropEvent event);
}
