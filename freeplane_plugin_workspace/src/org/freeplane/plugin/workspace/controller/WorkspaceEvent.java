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

	public static final int WORKSPACE_EVENT_TYPE_CHANGED = 1;
	public static final int WORKSPACE_EVENT_TYPE_TOOLBAR_EVENT = 2;
	public static final int WORKSPACE_EVENT_TYPE_RELOAD = 3;
	
	private static final long serialVersionUID = 1L;
	
	
	private final int type;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param source
	 */
	public WorkspaceEvent(final int type, final Object source) {
		super(source);
		this.type = type;
	}
	/**
	 * @return
	 */
	public int getType() {
		return this.type;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
