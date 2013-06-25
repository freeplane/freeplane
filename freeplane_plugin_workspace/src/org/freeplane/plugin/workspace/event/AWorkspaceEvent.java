/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.freeplane.plugin.workspace.event;

import java.util.EventObject;


/**
 * 
 */
public class AWorkspaceEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private transient boolean consumed = false;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param source
	 */
	public AWorkspaceEvent(final Object source) {
		super(source);
	}	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void consume() {
		this.consumed = true;
	}
	
	public boolean isConsumed() {
		return this.consumed;
	}
	
	
	/**
     * Returns a String representation of this Event.
     *
     * @return  a String representation of this Event.
     */
    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
