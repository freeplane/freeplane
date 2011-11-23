/**
 * author: Marcel Genzmehr
 * 21.10.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;

/**
 * 
 */
public interface IDropAcceptor {
	public boolean acceptDrop(DataFlavor[] flavors);
	public boolean processDrop(DropTargetDropEvent event);
	public boolean processDrop(Transferable transferable, int dropAction);
}
