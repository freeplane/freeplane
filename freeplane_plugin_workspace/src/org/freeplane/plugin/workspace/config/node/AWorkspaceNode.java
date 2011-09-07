package org.freeplane.plugin.workspace.config.node;

import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;


public abstract class AWorkspaceNode {
	final public static int WSNODE_DEFAULT_MODE = 0;
	
	private String name;
	private int currentMode;
	private final String type;
	private boolean system = false;
	
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
	
	
	@ExportAsAttribute("system")
	public boolean isSystem() {		
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
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
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		return false;
	}
	
	abstract public String getTagName();
	
	abstract public void initializePopup();
	
	public void setMandatoryAttributes(XMLElement data) {
		String system = data.getAttribute("system", "false");		
		if (system.equals("true")) {
			setSystem(true);
		}
	}
	

	/**
	 * @param renderer
	 * @param node
	 */
	

	
}
