/**
 * author: Marcel Genzmehr
 * 22.08.2011
 */
package org.docear.plugin.core;

import java.util.EventObject;

import org.docear.plugin.core.workspace.node.DocearConstants;

/**
 * 
 */
public class DocearEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final DocearConstants type;	
	private final Object eventObject;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public DocearEvent(Object source) {
		this(source, DocearConstants.NULL, null);
	}
	
	public DocearEvent(Object source, DocearConstants type) {
		this(source, type, null);
	}
	
	public DocearEvent(Object source, DocearConstants type, Object eventObj) {
		super(source);
		this.type = type;
		this.eventObject = eventObj;
	}
	
	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public DocearConstants getType() {
		return type;
	}
	
	public Object getEventObject() {
		return eventObject;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
