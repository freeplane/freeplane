/**
 * author: Marcel Genzmehr
 * 03.11.2011
 */
package org.freeplane.plugin.workspace.model;

import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AFolderNode;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;
import org.freeplane.plugin.workspace.config.node.VirtualFolderNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class WorkspaceIndexedTreeModel implements TreeModel {

	private AWorkspaceTreeNode root = null;
	private final HashMap<String, AWorkspaceTreeNode> hashStringKeyIndex = new HashMap<String, AWorkspaceTreeNode>();
	protected EventListenerList listenerList = new EventListenerList();

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public TreeModelListener[] getTreeModelListeners() {
		return (TreeModelListener[]) listenerList.getListeners(TreeModelListener.class);
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param source
	 *            the node being changed
	 * @param path
	 *            the path to the root node
	 * @param childIndices
	 *            the indices of the changed elements
	 * @param children
	 *            the changed elements
	 */
	protected void fireTreeNodesChanged(Object source, TreePath path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param source
	 *            the node where new elements are being inserted
	 * @param path
	 *            the path to the root node
	 * @param childIndices
	 *            the indices of the new elements
	 * @param children
	 *            the new elements
	 * @see EventListenerList
	 */
	protected void fireTreeNodesInserted(Object source, TreePath path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param source
	 *            the node where elements are being removed
	 * @param path
	 *            the path to the root node
	 * @param childIndices
	 *            the indices of the removed elements
	 * @param children
	 *            the removed elements
	 * @see EventListenerList
	 */
	protected void fireTreeNodesRemoved(Object source, TreePath path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param source
	 *            the node where the tree model has changed
	 * @param path
	 *            the path to the root node
	 * @param childIndices
	 *            the indices of the affected elements
	 * @param children
	 *            the affected elements
	 * @see EventListenerList
	 */
	protected void fireTreeStructureChanged(Object source, TreePath path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path, childIndices, children);
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param source
	 *            the node where the tree model has changed
	 * @param path
	 *            the path to the root node
	 * @see EventListenerList
	 */
	protected void fireTreeStructureChanged(Object source, TreePath path) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TreeModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TreeModelEvent(source, path);
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	public void reload(AWorkspaceTreeNode node) {
		if (node != null) {
			fireTreeStructureChanged(this, node.getTreePath(), null, null);
		}
	}

	/**
	 * Invoke this method if you've modified the TreeNodes upon which this model
	 * depends. The model will notify all of its listeners that the model has
	 * changed.
	 */
	public void reload() {
		reload(root);
	}

	/**
	 * Invoke this method after you've changed how node is to be represented in
	 * the tree.
	 */
	public void nodeChanged(AWorkspaceTreeNode node) {
		if (listenerList != null && node != null) {
			AWorkspaceTreeNode parent = node.getParent();

			if (parent != null) {
				int anIndex = parent.getIndex(node);
				if (anIndex != -1) {
					int[] cIndexs = new int[1];

					cIndexs[0] = anIndex;
					nodesChanged(parent, cIndexs);
				}
			}
			else if (node == getRoot()) {
				nodesChanged(node, null);
			}
		}
	}

	/**
	 * Invoke this method after you've inserted some TreeNodes into node.
	 * childIndices should be the index of the new elements and must be sorted
	 * in ascending order.
	 */
	public void nodesWereInserted(AWorkspaceTreeNode node, int[] childIndices) {
		if (listenerList != null && node != null && childIndices != null && childIndices.length > 0) {
			int cCount = childIndices.length;
			Object[] newChildren = new Object[cCount];

			for (int counter = 0; counter < cCount; counter++)
				newChildren[counter] = node.getChildAt(childIndices[counter]);
			fireTreeNodesInserted(this, node.getTreePath(), childIndices, newChildren);
		}
	}

	/**
	 * Invoke this method after you've removed some TreeNodes from node.
	 * childIndices should be the index of the removed elements and must be
	 * sorted in ascending order. And removedChildren should be the array of the
	 * children objects that were removed.
	 */
	public void nodesWereRemoved(AWorkspaceTreeNode node, int[] childIndices, Object[] removedChildren) {
		if (node != null && childIndices != null) {
			fireTreeNodesRemoved(this, node.getTreePath(), childIndices, removedChildren);
		}
	}

	/**
	 * Invoke this method after you've changed how the children identified by
	 * childIndicies are to be represented in the tree.
	 */
	public void nodesChanged(AWorkspaceTreeNode node, int[] childIndices) {
		if (node != null) {
			if (childIndices != null) {
				int cCount = childIndices.length;

				if (cCount > 0) {
					Object[] cChildren = new Object[cCount];

					for (int counter = 0; counter < cCount; counter++)
						cChildren[counter] = node.getChildAt(childIndices[counter]);
					fireTreeNodesChanged(this, node.getTreePath(), childIndices, cChildren);
				}
			}
			else if (node == getRoot()) {
				fireTreeNodesChanged(this, node.getTreePath(), null, null);
			}
		}
	}

	/**
	 * Invoke this method if you've totally changed the children of node and its
	 * childrens children... This will post a treeStructureChanged event.
	 */
	public void nodeStructureChanged(AWorkspaceTreeNode node) {
		if (node != null) {
			fireTreeStructureChanged(this, node.getTreePath(), null, null);
		}
	}
 
	/**
	 * @param key
	 * @return
	 */
	public boolean containsNode(String key) {
		return this.hashStringKeyIndex.containsKey(key);
	}
	
	/**
	 * @param key
	 * @return
	 */
	public AWorkspaceTreeNode getNode(String key) {
		return this.hashStringKeyIndex.get(key);
	}
	
	
	/**
	 * @param node
	 * @param targetNode
	 */
	public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode) {
		node.setParent(targetNode);
		if(this.containsNode(node.getKey())) {
			return false;
		}
		targetNode.addChildNode(node);
		this.hashStringKeyIndex.put(node.getKey(), node);
		return true;
	}
	
	public void removeAllElements(AWorkspaceTreeNode node) {
		Enumeration<AWorkspaceTreeNode> children = node.children();
		AWorkspaceTreeNode child = null;
		while(children.hasMoreElements()) {
			child = children.nextElement();
			this.hashStringKeyIndex.remove(child.getKey());
			child.disassociateReferences();
		}
		node.removeAllChildren();
	}
	
	/**
	 * @param node
	 */
	public void removeNodeFromParent(AWorkspaceTreeNode node) {
		this.hashStringKeyIndex.remove(node.getKey());
		node.getParent().removeChildNode(node);
		node.disassociateReferences();
		
	}
	
	/**
	 * @param node
	 * @param targetNode
	 */
	public void moveNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode) {
		
		AWorkspaceTreeNode oldParent = node.getParent();
		oldParent.removeChildNode(node);
		fireTreeStructureChanged(this, oldParent.getTreePath());
		
		targetNode.addChildNode(node);
		if(node instanceof AFolderNode) {
			((AFolderNode)node).refresh();
		}
		fireTreeStructureChanged(this, targetNode.getTreePath());
	}
	
	/**
	 * @param node
	 * @param targetNode
	 */
	public void copyNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode) {
		// TODO implement better method
		appendNodeCopyRecursive(targetNode, node);
	}
	
	/**
	 * 
	 */
	public void resetIndex() {
		this.hashStringKeyIndex.clear();		
	}
	
	
	private void appendNodeCopyRecursive(AWorkspaceTreeNode targetNode, AWorkspaceTreeNode node) {			
		AWorkspaceTreeNode newTarget = node.clone();
		newTarget.setParent(targetNode);
		if(WorkspaceUtils.getModel().containsNode(newTarget.getKey())) {
			newTarget = WorkspaceUtils.getModel().getNode(newTarget.getKey());
		} 
		else {
			targetNode.addChildNode(newTarget);
		}
		
		if(node instanceof VirtualFolderNode) {
			Enumeration<AWorkspaceTreeNode> children = node.children();
			while(children.hasMoreElements()) {
				appendNodeCopyRecursive(newTarget, children.nextElement());
			}
		} 
		else 
		if(node instanceof PhysicalFolderNode) {
			PhysicalFolderNode phyNode = (PhysicalFolderNode) node;
			WorkspaceController.getController().getFilesystemReader()
					.scanFileSystem(phyNode, WorkspaceUtils.resolveURI(phyNode.getFolderPath()));			
		} 
		else 
		if(node instanceof AFolderNode) {
			((AFolderNode) node).refresh();
		}
		
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public Object getRoot() {
		return root;
	}
	
	public void setRoot(AWorkspaceTreeNode object) {
		this.root = object;
	}

	public Object getChild(Object parent, int index) {
		return ((AWorkspaceTreeNode) parent).getChildAt(index);
	}

	public int getChildCount(Object parent) {
		return ((AWorkspaceTreeNode) parent).getChildCount();
	}

	public boolean isLeaf(Object node) {
		return ((AWorkspaceTreeNode) node).isLeaf();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();
		if (node instanceof IWorkspaceNodeEventListener) {
			((IWorkspaceNodeEventListener) node).handleEvent(new WorkspaceNodeEvent(node, WorkspaceNodeEvent.WSNODE_CHANGED,
					newValue));
			nodeChanged(node);
		}
		else {
			node.setName(newValue.toString());
		}
	}

	public int getIndexOfChild(Object parent, Object child) {
		return ((AWorkspaceTreeNode) parent).getIndex((TreeNode) child);
	}

	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}	
}
