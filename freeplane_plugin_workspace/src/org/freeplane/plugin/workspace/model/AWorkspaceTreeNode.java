package org.freeplane.plugin.workspace.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.docear.lang.Destructable;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;


public abstract class AWorkspaceTreeNode implements Cloneable, TreeNode, Destructable, Serializable {

	private static final long serialVersionUID = 1L;

	public final static int WSNODE_DEFAULT_MODE = 0;
	
	/**
     * An enumeration that is always empty. This is used when an enumeration
     * of a leaf node's children is requested.
     */
    static public final Enumeration<TreeNode> EMPTY_ENUMERATION
	= new Enumeration<TreeNode>() {
	    public boolean hasMoreElements() { return false; }
	    public TreeNode nextElement() {
		throw new NoSuchElementException("No more elements");
	    }
    };
	
	// needed for TreeNode interface
	private AWorkspaceTreeNode parent = null;
	private ArrayList<AWorkspaceTreeNode> children = new ArrayList<AWorkspaceTreeNode>(); 
	//private ArrayList<AWorkspaceTreeNode> children = new ArrayList<AWorkspaceTreeNode>();
	private boolean allowsChildren = true;
	private TreePath path = new WorkspaceTreeNodePath(this);
	
	//for workspace nodes
	private String name;
	private int currentMode;
	private final String type;
	private boolean system = false;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public AWorkspaceTreeNode(final String type) {
		this.type = type;
		this.currentMode = WSNODE_DEFAULT_MODE;
	}
		
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	abstract public AWorkspaceTreeNode clone();
	
	abstract public String getTagName();
	
	abstract public void initializePopup();
	
	
	public void setParent(AWorkspaceTreeNode node) {
		this.parent = node;
		if(node == null) {
			return;
		}
		path = node.getTreePath().pathByAddingChild(this);
	}

	public TreePath getTreePath() {
		return path;
	}


	public void allowChildren(boolean allow) {
		allowsChildren = allow;
	}
	
	public void addChildNode(AWorkspaceTreeNode node) {
		node.setParent(this);
		children.add(node);
	}
	
	@ExportAsAttribute("system")
	public boolean isSystem() {		
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
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
	
	public final String getKey() {
		return getTreePath().toString();
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
		
	public void setMandatoryAttributes(XMLElement data) {
		String system = data.getAttribute("system", "false");		
		if (system.equals("true")) {
			setSystem(true);
		}
	}
	
	public void removeAllChildren() {
		for(int i=0; i < children.size(); ) {  
			children.remove(i);
		}
	}
	
	public void removeChild(AWorkspaceTreeNode child) {
		children.remove(child);
	}
	
	/**
	 * @param node
	 */
	public void removeChildNode(AWorkspaceTreeNode node) {
		children.remove(node);		
	}
	
	protected AWorkspaceTreeNode clone(AWorkspaceTreeNode node) {		
		node.allowChildren(this.getAllowsChildren());
		node.setMode(getMode());
		node.setSystem(isSystem());
		node.setParent(getParent());
		node.setName(getName());
		for(AWorkspaceTreeNode child : this.children) {
			//FIXME: the model should handle this 
			node.addChildNode(child.clone());
		}		
		return node;
	}	
	
	public String toString() {
		return this.getClass().getSimpleName()+"[type="+this.getType()+";name="+this.getName()+"]";
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public AWorkspaceTreeNode getChildAt(int childIndex) {
		return children.get(childIndex); 
	}

	public int getChildCount() {
		return children.size();
	}

	public AWorkspaceTreeNode getParent() {
		return this.parent;
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public boolean getAllowsChildren() {
		return allowsChildren;
	}

	public boolean isLeaf() {
		return !allowsChildren  || (children.size() == 0);
	}
	
	public Enumeration<AWorkspaceTreeNode> children() {
		return new Enumeration<AWorkspaceTreeNode>() {
		    int count = 0;

		    public boolean hasMoreElements() {
		    	return count < children.size();
		    }

		    public AWorkspaceTreeNode nextElement() {
				synchronized (children) {
				    if (count < children.size()) {
				    	return (AWorkspaceTreeNode)children.get(count++);
				    }
				}
				throw new NoSuchElementException("AWorkspaceTreeNode Enumeration");
		    }
		};
	}

	public void disassociateReferences() {
		WorkspaceUtils.getModel().removeAllElements(this);
		this.parent = null;
	}

		
}
