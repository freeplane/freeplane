/**
 * author: Marcel Genzmehr
 * 22.08.2011
 */
package org.docear.plugin.core.event;

import java.util.EventObject;


/**
 * 
 */
public class DocearEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final DocearEventType type;	
	private final Object eventObject;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public DocearEvent(Object source) {
		this(source, DocearEventType.NULL, null);
	}
	
	public DocearEvent(Object source, DocearEventType type) {
		this(source, type, null);
	}
	
	public DocearEvent(Object source, DocearEventType type, Object eventObj) {
		super(source);
		this.type = type;
		this.eventObject = eventObj;
	}
	
	public DocearEvent(Object source, Object eventObj) {
		this(source, DocearEventType.NULL , eventObj);
	}
	
	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public DocearEventType getType() {
		return type;
	}
	
	public Object getEventObject() {
		return eventObject;
	}
	
	public String toString() {
		return this.getClass().getSimpleName()+"[type="+getType()+";eventObject="+getEventObject()+";source="+getSource()+"]";
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
