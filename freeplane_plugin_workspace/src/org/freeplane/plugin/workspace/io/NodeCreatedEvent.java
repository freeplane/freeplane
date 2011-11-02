/**
 * author: Marcel Genzmehr
 * 26.10.2011
 */
package org.freeplane.plugin.workspace.io;


public class NodeCreatedEvent {
	public enum NodeCreatedType {
		NODE_TYPE_FOLDER, NODE_TYPE_FILE
	}
	private final Object sourceKey;
	private final Object newKey;
	private final NodeCreatedType type;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public NodeCreatedEvent(Object targetKey, Object newKey, NodeCreatedType type) {
		assert(targetKey != null);
		assert(newKey != null);
		assert (type != null);
		
		this.sourceKey = targetKey;
		this.newKey = newKey;
		this.type = type;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public Object getTargetKey() {
		return sourceKey;
	}

	public Object getNewKey() {
		return newKey;
	}
	
	public NodeCreatedType getType() {
		return type;
	}

	public String toString() {
		return this.getClass().getSimpleName()+"[target="+getTargetKey()+";new="+getNewKey()+"]";
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
