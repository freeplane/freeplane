/**
 * author: Marcel Genzmehr
 * 03.11.2011
 */
package org.freeplane.plugin.workspace.model.project;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent.WorkspaceModelEventType;
import org.freeplane.plugin.workspace.model.WorkspaceModelException;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModel;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.ALinkNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.FolderFileNode;

public class ProjectModel implements WorkspaceTreeModel {

	private AWorkspaceTreeNode root = null;
	private final Map<String, AWorkspaceTreeNode> hashStringKeyIndex = new HashMap<String, AWorkspaceTreeNode>();
	protected EventListenerList listenerList = new EventListenerList();
	private final AWorkspaceProject PROJECT;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ProjectModel(AWorkspaceProject project) {
		this.PROJECT = project;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public AWorkspaceProject getProject() {
		return this.PROJECT;
	}
	
	public IProjectModelListener[] getTreeModelListeners() {
		return (IProjectModelListener[]) listenerList.getListeners(IProjectModelListener.class);
	}

	protected void fireTreeNodesChanged(Object source, TreePath path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				// Lazily create the event:
				if (e == null) e = new WorkspaceModelEvent(getProject(), source, path, childIndices, children);
				((IProjectModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}
	
	protected void fireTreeNodeChanged(Object source, TreePath path, int childIndex, Object children, Object oldValue, Object newValue) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = new WorkspaceModelEvent(getProject(), source, path, WorkspaceModelEventType.DEFAULT, oldValue, newValue);
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				((IProjectModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}

	protected void fireTreeNodesInserted(Object source, TreePath path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				// Lazily create the event:
				if (e == null) e = new WorkspaceModelEvent(getProject(), source, path, childIndices, children);
				((IProjectModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}

	protected void fireTreeNodesRemoved(Object source, TreePath path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				// Lazily create the event:
				if (e == null) e = new WorkspaceModelEvent(getProject(), source, path, childIndices, children);
				((IProjectModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}

	protected void fireTreeNodesRemoved(Object source, TreePath path, Object from, Object to) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				// Lazily create the event:
				if (e == null) e = new WorkspaceModelEvent(getProject(), source, path, WorkspaceModelEventType.DELETED, from, to);
				((IProjectModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}

	protected void fireTreeStructureChanged(Object source, TreePath path, int[] childIndices, Object[] children) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				// Lazily create the event:
				if (e == null) e = new WorkspaceModelEvent(getProject(), source, path, childIndices, children);
				((IProjectModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	protected void fireTreeStructureChanged(Object source, TreePath path) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				// Lazily create the event:
				if (e == null) e = new WorkspaceModelEvent(getProject(), source, path);
				((IProjectModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	protected void fireTreeStructureMoved(Object source, TreePath path, Object from, Object to) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				// Lazily create the event:
				if (e == null) e = new WorkspaceModelEvent(getProject(), source, path, WorkspaceModelEventType.MOVED, from, to);
				((IProjectModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	protected void fireTreeNodeRenamed(Object source, TreePath path, Object from, Object to) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		WorkspaceModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IProjectModelListener.class) {
				// Lazily create the event:
				if (e == null) e = new WorkspaceModelEvent(getProject(), source, path, WorkspaceModelEventType.RENAMED, from, to);
				((IProjectModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}
	
	public void reload(AWorkspaceTreeNode node) {
		if (node != null) {
			fireTreeStructureChanged(this, node.getTreePath(), null, null);
		}
	}

	public void reload() {
		reload(root);
	}

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
	
	public void nodeChanged(AWorkspaceTreeNode node, Object oldValue, Object newValue) {
		fireTreeNodeChanged(this, node.getTreePath(), node.getParent().getChildIndex(node), node, oldValue, newValue);		
	}

	public void nodeMoved(AWorkspaceTreeNode node, Object from, Object to) {
		fireTreeStructureMoved(this, node.getTreePath(), from, to);
	}

	public void nodesWereInserted(AWorkspaceTreeNode node, int[] childIndices) {
		if (listenerList != null && node != null && childIndices != null && childIndices.length > 0) {
			int cCount = childIndices.length;
			Object[] newChildren = new Object[cCount];

			for (int counter = 0; counter < cCount; counter++)
				newChildren[counter] = node.getChildAt(childIndices[counter]);
			fireTreeNodesInserted(this, node.getTreePath(), childIndices, newChildren);
		}
	}

	public void nodesWereRemoved(AWorkspaceTreeNode node, int[] childIndices, Object[] removedChildren) {
		if (node != null && childIndices != null) {
			fireTreeNodesRemoved(this, node.getTreePath(), childIndices, removedChildren);
		}
	}

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
	 * @return
	 */
	public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode) {
		return addNodeTo(node, targetNode, true);
	}

	/**
	 * @param node
	 * @param targetNode
	 * @param allowRenaming
	 * @return
	 */
	public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode, boolean allowRenaming) {
		return insertNodeTo(node, targetNode, targetNode.getChildCount(), allowRenaming);
	}

	/**
	 * @param node
	 * @param targetNode
	 * @param allowRenaming
	 * @return
	 */
	public boolean insertNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode, int atPos, boolean allowRenaming) {
		if(node.equals(targetNode)) {
			return false;
		}
		node.setParent(targetNode);
		// DOCEAR - look for problems that may caused by this change!!!
		if (allowRenaming) {
			String newNodeName = node.getName();
			int nameCount = 0;
			while (this.containsNode(node.getKey()) && nameCount++ < 100) {
				node.setName(newNodeName + " (" + nameCount + ")");
			}
		}
		if (this.containsNode(node.getKey())) {
			return false;
		}

		targetNode.insertChildNode(node, atPos);		
		nodesWereInserted(targetNode, new int[] { atPos });
		addToIndexRecursively(node, targetNode);

		return true;
	}

	private void addToIndexRecursively(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode) {
		this.hashStringKeyIndex.put(node.getKey(), node);
		if (node.getChildCount() > 0) {
			int[] indices = new int[node.getChildCount()];
			for (int i = 0; i < node.getChildCount(); i++) {
				AWorkspaceTreeNode childNode = node.getChildAt(i);
				addToIndexRecursively(childNode, node);
				indices[i] = targetNode.getChildCount() - 1;
			}
			nodesWereInserted(targetNode, indices);
		}
	}

	/**
	 * @param node
	 */
	public void removeAllElements(AWorkspaceTreeNode node) {
		Enumeration<AWorkspaceTreeNode> children = node.children();
		AWorkspaceTreeNode child = null;
		while (children.hasMoreElements()) {
			child = children.nextElement();
			this.hashStringKeyIndex.remove(child.getKey());
			child.disassociateReferences();
			fireTreeNodesRemoved(this, node.getTreePath(), null, new Object[] { child });
		}
		node.removeAllChildren();

	}

	/**
	 * @param node
	 */
	public void removeNodeFromParent(AWorkspaceTreeNode node) {
		this.hashStringKeyIndex.remove(node.getKey());
		AWorkspaceTreeNode parent = node.getParent();
		parent.removeChild(node);
		node.disassociateReferences();
		fireTreeNodesRemoved(this, parent.getTreePath(), null, new Object[] { node });
	}

	/**
	 * @param node
	 */
	public void cutNodeFromParent(AWorkspaceTreeNode node) {
		AWorkspaceTreeNode parent = node.getParent();
		removeFromIndexRecursively(node);
		parent.removeChild(node);
		fireTreeNodesRemoved(this, parent.getTreePath(), null, new Object[] { node });
	}

	/**
	 * @param node
	 */
	private void removeFromIndexRecursively(AWorkspaceTreeNode node) {
		List<AWorkspaceTreeNode> removes = new ArrayList<AWorkspaceTreeNode>();
		this.hashStringKeyIndex.remove(node.getKey());
		if (node.getChildCount() > 0) {
			int[] indices = new int[node.getChildCount()];
			for (int i = 0; i < node.getChildCount(); i++) {
				AWorkspaceTreeNode childNode = node.getChildAt(i);
				removeFromIndexRecursively(childNode);
				removes.add(childNode);
				indices[i] = i;
			}
			fireTreeNodesRemoved(this, node.getTreePath(), indices, removes.toArray());
		}
	}
	
	/**
	 * @param node
	 */
	private void removeIndexOnlyRecursively(AWorkspaceTreeNode node) {
		this.hashStringKeyIndex.remove(node.getKey());
		if (node.getChildCount() > 0) {
			for (int i = 0; i < node.getChildCount(); i++) {
				AWorkspaceTreeNode childNode = node.getChildAt(i);
				removeIndexOnlyRecursively(childNode);
			}
		}
	}
	
	private void addIndexOnlyRecursively(AWorkspaceTreeNode node) {
		this.hashStringKeyIndex.put(node.getKey(), node);
		if (node.getChildCount() > 0) {
			for (int i = 0; i < node.getChildCount(); i++) {
				AWorkspaceTreeNode childNode = node.getChildAt(i);
				addIndexOnlyRecursively(childNode);
			}
		}
	}

	public void changeNodeName(AWorkspaceTreeNode node, String newName) throws WorkspaceModelException {
		String oldName = node.getName();
		node.setName(newName);
		if (this.hashStringKeyIndex.containsKey(node.getKey())) {
			node.setName(oldName);
			throw new WorkspaceModelException("A Node with the name '" + newName + "' already exists.");
		}
		node.setName(oldName);
		removeIndexOnlyRecursively(node);
		node.setName(newName);
		addIndexOnlyRecursively(node);
		
		fireTreeNodeRenamed(this, node.getTreePath(), oldName, newName);
	}

	/**
	 * 
	 */
	public void resetIndex() {
		this.hashStringKeyIndex.clear();
	}

	public List<URI> getAllNodesFiltered(String filter) {
		HashSet<URI> set = new HashSet<URI>();
		Collection<AWorkspaceTreeNode> values = hashStringKeyIndex.values();
		for (AWorkspaceTreeNode node : values) {

			if (node instanceof AFolderNode || node instanceof FolderFileNode) {
				continue;
			}

			if (node instanceof DefaultFileNode) {
				File file = ((DefaultFileNode) node).getFile();
				if (file.getName().endsWith(filter)) {
					set.add(file.toURI());
				}
			}
			else if (node instanceof ALinkNode) {
				URI uri = ((ALinkNode) node).getLinkURI();
				if (uri.getPath().endsWith(filter)) {
					URI absUri = URIUtils.getAbsoluteURI(uri);
					if(absUri == null) {
						continue;
					}
					set.add(absUri);
				}
			}
		}
		return Arrays.asList(set.toArray(new URI[] {}));
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public AWorkspaceTreeNode getRoot() {
		return root;
	}

	public void setRoot(AWorkspaceTreeNode newRoot) {
		AWorkspaceTreeNode oldRoot = this.root; 
		this.root = newRoot;
		if(this.root != null) {
			this.root.setModel(this);
			if(oldRoot == null) {
				fireTreeNodesInserted(this, null, new int[]{}, new Object[]{root});
			}
			else {
				fireTreeNodesChanged(this, null, new int[]{}, new Object[]{root});
			}
		}
		else {
			if(oldRoot != null) {
				fireTreeNodesRemoved(this, null, new int[]{}, new Object[]{oldRoot});
			}
		}
		
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
		if (node instanceof IWorkspaceNodeActionListener) {
			((IWorkspaceNodeActionListener) node).handleAction(new WorkspaceActionEvent(node, WorkspaceActionEvent.WSNODE_CHANGED, newValue));
			nodeChanged(node);
		}
		else {
			node.setName(newValue.toString());
		}
	}

	public int getIndexOfChild(Object parent, Object child) {
		return ((AWorkspaceTreeNode) parent).getIndex((TreeNode) child);
	}

	public void addProjectModelListener(IProjectModelListener listener) {
		if(listener == null) {
			return;
		}
		listenerList.add(IProjectModelListener.class, listener);
	}

	public void removeProjectModelListener(IProjectModelListener listener) {
		if(listener == null) {
			return;
		}
		listenerList.remove(IProjectModelListener.class, listener);
	}

	public void requestSave() {
		try {
			WorkspaceController.getCurrentModeExtension().getProjectLoader().storeProject(WorkspaceController.getCurrentModel().getProject(this));
		} catch (IOException e) {
			LogUtils.severe(e);
		}
		
	}
}
