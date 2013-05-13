package org.freeplane.plugin.workspace.model;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent.WorkspaceModelEventType;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.IProjectModelListener;
import org.freeplane.plugin.workspace.model.project.ProjectModel;
import org.freeplane.plugin.workspace.nodes.WorkspaceRootNode;

public abstract class WorkspaceModel implements TreeModel {	
	
	protected List<AWorkspaceProject> projects = new ArrayList<AWorkspaceProject>();
	protected final List<WorkspaceModelListener> listeners = new ArrayList<WorkspaceModelListener>();
	
	protected WorkspaceRootNode root;
	protected IProjectModelListener projectModelListener;
	
	public void addProject(AWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (projects) {
			if(!projects.contains(project)) {
				projects.add(project);
				project.getModel().addProjectModelListener(getTreeModelListener());
				fireProjectInserted(project);				
			}
		}
	}
	
	public void removeProject(AWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (projects) {
			int index = getProjectIndex(project);
			if(index > -1) {
				projects.remove(project);
				project.getModel().removeProjectModelListener(getTreeModelListener());
				fireProjectRemoved(project, index);
				
			}
		}
	}
	
	private WorkspaceModelEvent validatedModelEvent(WorkspaceModelEvent event) {
		WorkspaceModelEvent evt = event; 
		if(event.getTreePath() == null) {
			int[] indices;
			int index = getProjectIndex(event.getProject());
			if(index < 0) {
				indices = new int[]{};
			}
			else {
				indices = new int[]{index};
			}
			evt = new WorkspaceModelEvent(event.getProject(), event.getSource(), getRoot().getTreePath(), indices, event.getChildren());
		}
		return evt;
	}
	
	public int getProjectIndex(AWorkspaceProject project) {
		synchronized (projects) {
			int index = 0;
			for (AWorkspaceProject prj : projects) {
				if(prj.equals(project)) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}
	
	protected void resetClipboard() {
		if(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null) instanceof WorkspaceTransferable) {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(null, null);
			LogUtils.info("clipboard has been reset");
		}
	}

	protected IProjectModelListener getTreeModelListener() {
		if(projectModelListener == null) {
			projectModelListener = new IProjectModelListener() {
				
				public void treeStructureChanged(WorkspaceModelEvent event) {
					resetClipboard();
					event = validatedModelEvent(event);
					synchronized (listeners) {
						for (int i = listeners.size()-1; i >= 0; i--) {
							listeners.get(i).treeStructureChanged(event);
						}
					}
				}
				
				public void treeNodesRemoved(WorkspaceModelEvent event) {
					event = validatedModelEvent(event);
					synchronized (listeners) {
						for (int i = listeners.size()-1; i >= 0; i--) {
							listeners.get(i).treeNodesRemoved(event);
						}
					}
				}
				
				public void treeNodesInserted(WorkspaceModelEvent event) {
					event = validatedModelEvent(event);
					synchronized (listeners) {						
						for (int i = listeners.size()-1; i >= 0; i--) {
							listeners.get(i).treeNodesInserted(event);
						}
					}
				}
				
				public void treeNodesChanged(WorkspaceModelEvent event) {
					event = validatedModelEvent(event);
					synchronized (listeners) {
						for (int i = listeners.size()-1; i >= 0; i--) {
							listeners.get(i).treeNodesChanged(event);
						}
					}
				}
			};
		}
		return projectModelListener;
	}

	
	protected void fireProjectRemoved(AWorkspaceProject project, int index) {
		synchronized (listeners) {
			WorkspaceModelEvent event = new WorkspaceModelEvent(project, this, new Object[]{getRoot()}, new int[]{index}, new Object[]{project.getModel().getRoot()});
			for (int i = listeners.size()-1; i >= 0; i--) {
				WorkspaceModelListener listener = listeners.get(i); 
				listener.projectRemoved(event);
			}
		}
	}

	protected void fireProjectInserted(AWorkspaceProject project) {
		synchronized (listeners) {
			WorkspaceModelEvent event = new WorkspaceModelEvent(project, this, new Object[]{getRoot()}, new int[]{getProjectIndex(project)}, new Object[]{project.getModel().getRoot()});;
			for (int i = listeners.size()-1; i >= 0; i--) {
				listeners.get(i).projectAdded(event);
			}
		}		
	}
	
	protected void fireWorkspaceRenamed(String oldName, String newName) {
		synchronized (listeners) {
			TreeModelEvent event = new WorkspaceModelEvent(null, this, new Object[]{getRoot()}, new int[]{}, new Object[]{});
			for (int i = listeners.size()-1; i >= 0; i--) {
				listeners.get(i).treeNodesChanged(event);
			}
		}		
	}

	public AWorkspaceTreeNode getRoot() {
		if(root == null) {
			root = new WorkspaceRootNode();
			root.setModel(new DefaultWorkspaceTreeModel());
		}
		return root;
	}

	public Object getChild(Object parent, int index) {
		if(parent == getRoot()) {
			int offset = getRoot().getChildCount()-projects.size();
			if(index < offset) {
				return getRoot().getChildAt(index);
			} 
			else {
				synchronized (projects) {				
					AWorkspaceTreeNode node = projects.get(index-offset).getModel().getRoot();
					return node;
				}
			}
		}
		else {
			return ((AWorkspaceTreeNode) parent).getChildAt(index);
		}
	}

	public int getChildCount(Object parent) {
		if(parent == getRoot()) {
			return getRoot().getChildCount();
		}
		else {
			return ((AWorkspaceTreeNode) parent).getChildCount();
		}
	}

	public boolean isLeaf(Object node) {
		if(node == getRoot()) {
			return false;
		}
		else {
			return ((TreeNode) node).isLeaf();
		}
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		if(path == null || getRoot().equals(path.getLastPathComponent())) {
			return;
		}
		
		AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();
		if (node instanceof IWorkspaceNodeActionListener) {
			((IWorkspaceNodeActionListener) node).handleAction(new WorkspaceActionEvent(node, WorkspaceActionEvent.WSNODE_CHANGED, newValue));
			((ProjectModel) node.getModel()).nodeChanged(node);
		}
		else {
			node.setName(newValue.toString());
		}
	}

	public int getIndexOfChild(Object parent, Object child) {
		if(parent == getRoot()) {
			int offset = getRoot().getChildCount();
			if(offset > 0) {
				int index = getRoot().getChildIndex((AWorkspaceTreeNode) child);
				if( index > -1) {
					return index;
				}
			}
			synchronized (projects) {
				int index = 0;
				for (AWorkspaceProject project : projects) {
					if(child.equals(project.getModel().getRoot())) {
						return index+offset;
					}
					index++;
				}
				return -1;
			}
		}
		else {
			return ((AWorkspaceTreeNode) parent).getIndex((TreeNode) child);
		}
	}

	public void addTreeModelListener(TreeModelListener l) {
		if(l == null) {
			return;
		}
		WorkspaceModelListener listener = new WrappedTreeModelListener(l);
		synchronized (listeners) {
			if(listeners.contains(listener)) {
				return;
			}
			listeners.add(listener);
		}
	}
	
	public void addWorldModelListener(WorkspaceModelListener l) {
		if(l == null) {
			return;
		}
		synchronized (listeners) {
			if(listeners.contains(l)) {
				return;
			}
			listeners.add(l);
		}
	}

	public void removeTreeModelListener(TreeModelListener l) {
		if(l == null) {
			return;
		}
		WorkspaceModelListener listener = new WrappedTreeModelListener(l);
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public List<AWorkspaceProject> getProjects() {
		return this.projects;
	}

	public static WorkspaceModel createDefaultModel() {
		return new WorkspaceModel() {
		};
	}

	public AWorkspaceProject getProject(WorkspaceTreeModel model) {
		synchronized (this.projects) {
    		for (AWorkspaceProject project : this.projects) {
    			if(project.getModel().equals(model)) {
    				return project;
    			}
    		}
		}
		return null;
	}

	public AWorkspaceProject getProject(String projectID) {
		synchronized (this.projects) {
    		for (AWorkspaceProject project : this.projects) {
    			if(project.getProjectID().equals(projectID)) {
    				return project;
    			}
    		}
		}
		return null;
	}
	/**********************************************************************
	 * NESTED CLASSES
	 **********************************************************************/
	
	/**
	 * 
	 * @author mag
	 *
	 */
	protected final class DefaultWorkspaceTreeModel implements WorkspaceTreeModel {
		public void removeNodeFromParent(AWorkspaceTreeNode node) {
			if(node == null) {
				return;
			}
			
			if(getRoot().equals(node.getParent())) {
				//forbidden: use removeProject
			}
			else {
				WorkspaceTreeModel tModel = node.getModel();
				if(tModel == this) {
					AWorkspaceTreeNode parent = node.getParent();
					int index = parent.getChildIndex(node);
					parent.removeChild(node);
					node.disassociateReferences();
					WorkspaceModelEvent event = new WorkspaceModelEvent(null, this, parent.getTreePath(), new int[]{index}, new Object[]{node});
					getTreeModelListener().treeNodesRemoved(event);
				}
				else {
					tModel.removeNodeFromParent(node);
				}
			}
		}

		public void removeAllElements(AWorkspaceTreeNode node) {
			if(node == null) {
				return;
			}
			
			if(getRoot().equals(node)) {
				//forbidden: use removeProject
			}
			else {
				WorkspaceTreeModel tModel = node.getModel();
				if(tModel == this) {
					int[] indices = new int[node.getChildCount()];
					Object[] childArray = new Object[node.getChildCount()];
					Enumeration<AWorkspaceTreeNode> children = node.children();
					AWorkspaceTreeNode child = null;
					int i = 0;
					while (children.hasMoreElements()) {
						child = children.nextElement();
						child.disassociateReferences();
						indices[i] = i;
						childArray[i] = child;
						//fireTreeNodesRemoved(this, node.getTreePath(), null, new Object[] { child });
						i++;
					}
					WorkspaceModelEvent event = new WorkspaceModelEvent(null, this, node.getTreePath(), indices, childArray);
					getTreeModelListener().treeNodesRemoved(event);
					node.removeAllChildren();
				}
				else {
					tModel.removeAllElements(node);
				}
			}
		}

		public void reload(AWorkspaceTreeNode node) {
			if(node == null) {
				return;
			}
			resetClipboard();
			if(getRoot().equals(node)) {
				for (AWorkspaceProject project : getProjects()) {
					project.getModel().reload();
				}
			}
			else {
				WorkspaceTreeModel tModel = node.getModel();
				if(tModel == this) {
					WorkspaceModelEvent event = new WorkspaceModelEvent(null, this, node.getTreePath(), null, null);
					getTreeModelListener().treeStructureChanged(event);
					//fireTreeStructureChanged(this, node.getTreePath(), null, null);
				}
				else {
					tModel.reload(node);
				}
			}			
		}

		public void nodeMoved(AWorkspaceTreeNode node, Object from, Object to) {
			if(getRoot().equals(node)) {
				//forbidden for root node
			}
			else {
				WorkspaceTreeModel tModel = node.getModel();
				if(tModel == this) {
					WorkspaceModelEvent event = new WorkspaceModelEvent(null, this, node.getTreePath(), WorkspaceModelEventType.MOVED, from, to);
					getTreeModelListener().treeStructureChanged(event);
				}
				else {
					tModel.nodeMoved(node, from, to);
				}
			}
		}

		public boolean insertNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode, int atPos, boolean allowRenaming) {
			if(getRoot().equals(targetNode)) {
				//forbidden: only allowed via addProject()
				return false;
			}
			else {
				WorkspaceTreeModel tModel = node.getModel();
				if(tModel == this) {
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
					WorkspaceModelEvent event = new WorkspaceModelEvent(null, this, targetNode.getTreePath(), new int[]{atPos}, new Object[]{node});
					getTreeModelListener().treeNodesInserted(event);
					return true;
				}
				else {
					return tModel.insertNodeTo(node, targetNode, atPos, allowRenaming);
				}
			}
		}

		public AWorkspaceTreeNode getRoot() {
			return WorkspaceModel.this.getRoot();
		}

		public boolean containsNode(String key) {
			for (AWorkspaceProject project : getProjects()) {
				if(project.getModel().containsNode(key)) {
					return true;
				}
			}
			if(getRoot().getChildCount() > 0) {
				if(getChildByKey(getRoot(), key) != null) {
					return true;
				}
			}
			return false;
		}

		private AWorkspaceTreeNode getChildByKey(AWorkspaceTreeNode parent, String key) {
			if(key == null || key.isEmpty()) {
				return null;
			}
			Enumeration<AWorkspaceTreeNode> children = parent.children();
			AWorkspaceTreeNode child = null;
			while (children.hasMoreElements()) {
				child = children.nextElement();
				String childKey = child.getKey();
				if(key.startsWith(childKey)) {
					if(key.equals(childKey)) {
						return child;
					}
					else {
						return getChildByKey(child, key);
					}
				}
			}
			return null;
		}

		public AWorkspaceTreeNode getNode(String key) {
			if(getRoot().getKey().equals(key)) {
				return getRoot();
			}
			AWorkspaceTreeNode node = null;
			for (AWorkspaceProject project : getProjects()) {
				ProjectModel pModel = project.getModel();
				String pKey = pModel.getRoot().getKey(); 
				if(pKey.equals(key)) {
					node = pModel.getRoot();
					break;
				}
				else {
					node = pModel.getNode(key);
					if(node != null) {
						break; 
					}
				}
			}
			if(node == null && getRoot().getChildCount() > 0) {
				node = getChildByKey(getRoot(), key);
			}
			return node;
		}

		public void cutNodeFromParent(AWorkspaceTreeNode node) {
			if(getRoot().equals(node.getParent())) {
				//forbidden: use removeProject
			}
			else {
				WorkspaceTreeModel tModel = node.getModel();
				if(tModel == this) {
					AWorkspaceTreeNode parent = node.getParent();
					parent.removeChild(node);
					WorkspaceModelEvent event = new WorkspaceModelEvent(null, this, parent.getTreePath(), null, new Object[]{node});
					getTreeModelListener().treeNodesRemoved(event);
				}
				else {				
					tModel.cutNodeFromParent(node);
				}
			}
			
		}

		public void changeNodeName(AWorkspaceTreeNode node, String newName) throws WorkspaceModelException {
			if(getRoot().equals(node)) {
				String oldName = node.getName();
				node.setName(newName);				
				fireWorkspaceRenamed(oldName, newName);
			}
			else {
				WorkspaceTreeModel tModel = node.getModel();
				if(tModel == this) {
					String oldName = node.getName();
					node.setName(newName);
					if (containsNode(node.getKey())) {
						node.setName(oldName);
						throw new WorkspaceModelException("A Node with the name '" + newName + "' already exists.");
					}
					WorkspaceModelEvent event = new WorkspaceModelEvent(null, this, node.getTreePath(), WorkspaceModelEventType.RENAMED, oldName, newName);
					getTreeModelListener().treeNodesChanged(event);
				}
				else {
					tModel.changeNodeName(node, newName);
				}
			}			
		}

		public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode, boolean allowRenaming) {
			return insertNodeTo(node, targetNode, targetNode.getChildCount(), allowRenaming);
		}

		public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode) {
			return addNodeTo(node, targetNode, true);
		}

		public void requestSave() {
			WorkspaceController.save();
		}

		public void nodeChanged(AWorkspaceTreeNode node, Object oldValue, Object newValue) {
			if(getRoot().equals(node)) {
				//should not happen
			}
			else {
				WorkspaceTreeModel tModel = node.getModel();
				if(tModel == this) {
					WorkspaceModelEvent event = new WorkspaceModelEvent(null, this, node.getTreePath(), WorkspaceModelEventType.DEFAULT, oldValue, newValue);
					getTreeModelListener().treeNodesChanged(event);
				} else {
					node.getModel().nodeChanged(node, oldValue, newValue);
				}
			}			
		}
	}
	
	/**
	 * 
	 * @author mag
	 *
	 */	
	public class WrappedTreeModelListener implements WorkspaceModelListener {
		private final TreeModelListener wrappedListener;

		public WrappedTreeModelListener(TreeModelListener l) {
			if(l == null) {
				throw new IllegalArgumentException();
			}
			this.wrappedListener = l;
		}

		public void treeNodesChanged(TreeModelEvent e) {
			wrappedListener.treeNodesChanged(e);
		}

		public void treeNodesInserted(TreeModelEvent e) {
			wrappedListener.treeNodesInserted(e);
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			wrappedListener.treeNodesRemoved(e);
		}

		public void treeStructureChanged(TreeModelEvent e) {
			wrappedListener.treeStructureChanged(e);
		}

		public void projectAdded(WorkspaceModelEvent event) {
			//do nth.
		}

		public void projectRemoved(WorkspaceModelEvent event) {
			treeNodesRemoved(event);
		}
		
		public boolean equals(Object obj) {
			return super.equals(obj);
		}

	}
}
