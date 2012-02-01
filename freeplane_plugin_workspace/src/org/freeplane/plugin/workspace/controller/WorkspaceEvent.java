/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.util.EventObject;

/**
 * 
 */
public class WorkspaceEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	public static final WORKSPACE_EVENT_TYPE WORKSPACE_CHANGED = WORKSPACE_EVENT_TYPE.CHANGED;
	public static final WORKSPACE_EVENT_TYPE WORKSPACE_TOOLBAR_EVENT = WORKSPACE_EVENT_TYPE.TOOLBAR;
	public static final WORKSPACE_EVENT_TYPE WORKSPACE_RELOAD = WORKSPACE_EVENT_TYPE.RELOADED;
	
		
	
	private final WORKSPACE_EVENT_TYPE type;
	private transient boolean consumed = false;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param source
	 */
	public WorkspaceEvent(final WORKSPACE_EVENT_TYPE type, final Object source) {
		super(source);
		this.type = type;
	}	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	/**
	 * @return
	 */
	public WORKSPACE_EVENT_TYPE getType() {
		return this.type;
	}
	
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
        return getClass().getName() + "[type="+ type +";source=" + source + "]";
    }
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
