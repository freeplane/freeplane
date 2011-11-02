package org.freeplane.plugin.workspace.config.node;

import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.WorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspaceTreePath;


public abstract class AWorkspaceNode extends WorkspaceTreeNode {
	final public static int WSNODE_DEFAULT_MODE = 0;
	
	private String name;
	private int currentMode;
	private final String type;
	private boolean system = false;
	private String key;
	
	public AWorkspaceNode(final String type) {
		super(new WorkspaceTreePath());
		this.type = type;
		this.currentMode = WSNODE_DEFAULT_MODE;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		this.getTreePath().setName(getId());
	}
	
	public final String getId() {
		return Integer.toHexString(getName() == null ? "".hashCode() : getName().hashCode()).toUpperCase();
		//return Integer.toHexString(super.toString().hashCode()).toUpperCase();
	}
	
	public final String getKey() {
		//return super.getKey();
		if(key == null) {
			key = WorkspaceController.getController().getIndexTree().getKeyByUserObject(this).toString();
		}
		return key;
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
	
	//abstract public Object clone();
	
	abstract public void initializePopup();
	
	public void setMandatoryAttributes(XMLElement data) {
		String system = data.getAttribute("system", "false");		
		if (system.equals("true")) {
			setSystem(true);
		}
	}	
}
