package org.freeplane.plugin.workspace.config.node;

public abstract class WorkspaceNode {
	final public static int WSNODE_DEFAULT_MODE = 0;
	
	private String id;
	private String name;
	private int currentMode;
	
	public WorkspaceNode(String id) {
		this.id=id;
		this.name = id;
		this.currentMode = WSNODE_DEFAULT_MODE;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		return this.getClass().getSimpleName()+"[id="+this.getId()+";name="+this.getName()+"]";
	}
	
	public int getMode() {
		return this.currentMode;
	}
	
	public void setMode(int mode) {
		this.currentMode = mode;
	}
	
	abstract public String getTagName();
}
