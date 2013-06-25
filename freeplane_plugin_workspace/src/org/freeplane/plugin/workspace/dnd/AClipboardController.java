package org.freeplane.plugin.workspace.dnd;

import java.awt.datatransfer.ClipboardOwner;

public abstract class AClipboardController {

	private ClipboardOwner owner;
	private Object ownerLock = new Object();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public ClipboardOwner getClipboardOwner() {
		synchronized (ownerLock ) {
			return owner;
		}
	}

	public void resetClipboardOwner(ClipboardOwner oldOwner) {
		synchronized (ownerLock ) {
			if(owner == oldOwner) {
				owner = null;
			}
		}		
	}
	
	public void setClipboardOwner(ClipboardOwner newOwner) {
		synchronized (ownerLock ) {			
			owner = newOwner;
		}		
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
