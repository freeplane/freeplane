package org.freeplane.plugin.workspace.config.node;


public abstract class AWorkspaceNode {
	final public static int WSNODE_DEFAULT_MODE = 0;
	
	private String name;
	private int currentMode;
	private final String type;
	
	public AWorkspaceNode(final String type) {
		this.type = type;
		this.currentMode = WSNODE_DEFAULT_MODE;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public final String getId() {
		return Integer.toHexString(getName() == null ? "".hashCode() : getName().hashCode()).toUpperCase();
		//return Integer.toHexString(super.toString().hashCode()).toUpperCase();
	}
	
	public String toString() {
		return this.getClass().getSimpleName()+"[type="+this.getType()+";name="+this.getName()+"]";
	}
	
	public int getMode() {
		return this.currentMode;
	}
	
	public void setMode(int mode) {
		this.currentMode = mode;
	}
	
	public String getType() {
		return this.type;
	}
	
	public boolean isEditable() {
		return true;
	}
	
	abstract public String getTagName();

	
}
