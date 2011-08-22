/**
 * author: Marcel Genzmehr
 * 22.08.2011
 */
package org.docear.plugin.core;

import java.util.EventObject;

/**
 * 
 */
public class DocearEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final int type;	
	private final Object eventObject;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public DocearEvent(Object source) {
		this(source, -1, null);
	}
	
	public DocearEvent(Object source, int type) {
		this(source, type, null);
	}
	
	public DocearEvent(Object source, int type, Object eventObj) {
		super(source);
		this.type = type;
		this.eventObject = eventObj;
	}
	
	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public int getType() {
		return type;
	}
	
	public Object getEventObject() {
		return eventObject;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
