package org.freeplane.plugin.workspace.config.creator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

public abstract class AConfigurationNodeCreator implements IElementDOMHandler {
	
	abstract public AWorkspaceNode getNode(String id, XMLElement data);
			
	public AConfigurationNodeCreator() {
	}
	
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		final IndexedTree tree = WorkspaceController.getCurrentWorkspaceController().getTree();
		if (attributes == null) {
			return null;
		}
		String id = attributes.getAttribute("id", null);
		if (id == null) {
			if(WorkspaceCreator.class.isInstance(this)) {
//				tree.getRoot().setUserObject(getNode(id, attributes));
				Path path = new Path(parent == null ? null : parent.toString());
				path.setName("root");
				tree.addElement(path.parentPath == null ? tree : path.parentPath, this, path.path, IndexedTree.AS_CHILD);				
				return path;
			} 
			else {
				return parent == null ? Path.emptyPath() : parent;
			}
		}
		final Path path = new Path(parent == null ? null : parent.toString());
		path.setName(id);
		if (!tree.contains(path.path)) {
			tree.addElement(path.parentPath == null ? tree : path.parentPath, this, path.path, IndexedTree.AS_CHILD);
		}
		return path;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		final IndexedTree tree = WorkspaceController.getCurrentWorkspaceController().getTree();
		final String id = lastBuiltElement.getAttribute("id", null);
		final Path path = (Path)userObject;
		if (path.path == null) {
			return;
		}
		final DefaultMutableTreeNode treeNode = tree.get(path.path);
		if (treeNode.getUserObject() == this) {
			final AWorkspaceNode node = getNode(id, lastBuiltElement);
			LogUtils.info("AConfigurationNode.node,isNull? : "+(node==null));
			if(node != null) 
				treeNode.setUserObject(node);
			else 
				tree.removeElement(path.path);
		}
	}

	
	
	protected static class Path {
		static Path emptyPath() {
			final Path Path = new Path(null);
			Path.path = null;
			return Path;
		}

		String parentPath;
		String path;

		Path(final String path) {
			parentPath = path;
		}

		void setName(final String name) {
			path = parentPath == null ? name : parentPath + '/' + name;
		}

		@Override
		public String toString() {
			return path;
		}
	};
}