package org.freeplane.plugin.workspace.model;

import java.awt.Component;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.lang.Destructable;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;


public abstract class AWorkspaceTreeNode implements Cloneable, TreeNode, Destructable, Serializable {

	private static final long serialVersionUID = 1L;

	public final static int WSNODE_DEFAULT_MODE = 0;
		
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
	private boolean isTranferable = true;

	private WorkspaceTreeModel treeModel;
	
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
	
	public abstract WorkspacePopupMenu getContextMenu();
	
	
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
		insertChildNode(node, children.size());
	}
	
	public void insertChildNode(AWorkspaceTreeNode node, int atPos) {
		node.setParent(this);
		children.add(atPos, node);
	}
	
	@ExportAsAttribute(name="system")
	public boolean isSystem() {		
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}
	
	public void setTransferable(boolean enabled) {
		this.isTranferable = enabled;
	}
	
	/**
	 * @return
	 */
	@ExportAsAttribute(name="transferable",defaultBool=true)
	public boolean isTransferable() {
		return isTranferable;
	}

	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return Integer.toHexString(getName() == null ? "".hashCode() : getName().hashCode()).toUpperCase();
		//return Integer.toHexString(super.toString().hashCode()).toUpperCase();
		//return (getName() == null ? "NULL" : getName());
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
		return false;
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		return false;
	}
		
	public void setMandatoryAttributes(XMLElement data) {
		String system = data.getAttribute("system", "false");		
		if (system.equals("true")) {
			setSystem(true);
		}
		String transferable = data.getAttribute("transferable", "true");		
		if (transferable.equals("false")) {
			setTransferable(false);
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
	
	public int getChildIndex(AWorkspaceTreeNode child) {
		return children.indexOf(child);
	}
	
	public void refresh() {
		//override in child class, if necessary
		getModel().reload(this);
	}
	
	protected AWorkspaceTreeNode clone(AWorkspaceTreeNode node) {		
		node.allowChildren(this.getAllowsChildren());
		node.setMode(getMode());
		node.setSystem(isSystem());
		node.setParent(getParent());
		node.setName(getName());
		for(AWorkspaceTreeNode child : this.children) {
			node.addChildNode(child.clone());
		}		
		return node;
	}	
	
	public String toString() {
		return this.getClass().getSimpleName()+"[type="+this.getType()+";name="+this.getName()+"]";
	}
		
	public void showPopup(Component component, int x, int y) {		
		final WorkspacePopupMenu popupMenu = getContextMenu();
		if(popupMenu == null) {
			return;
		}
		popupMenu.setInvokerLocation(new Point(x, y));
		if (popupMenu != null) {
			popupMenu.show(component, x, y);
		}
	}
	
	public AWorkspaceTreeNode getChildById(String id) {
		if(id == null) {
			return null;
		}
		synchronized (children) {
			for (AWorkspaceTreeNode child : children) {
				if(id.equals(child.getId())) {
					return child;
				}
			}
		}
		return null;
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

	public boolean isLeaf() {
		return !allowsChildren  || (children.size() == 0);
	}
	
	public WorkspaceTreeModel getModel() {
		if(this.treeModel == null && getParent() != null) {
			return getParent().getModel();
		}
		return this.treeModel;
	}
	
	public void setModel(WorkspaceTreeModel model) {
		this.treeModel = model;
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
		getModel().removeAllElements(this);
		this.parent = null;
	}	
}
