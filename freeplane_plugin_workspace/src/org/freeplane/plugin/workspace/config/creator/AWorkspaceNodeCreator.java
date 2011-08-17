package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

public abstract class AWorkspaceNodeCreator implements IElementDOMHandler {
	
	abstract public AWorkspaceNode getNode(final XMLElement data);
	
			
	public AWorkspaceNodeCreator() {
	}
	
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		final IndexedTree tree = WorkspaceController.getCurrentWorkspaceController().getTree();
		if (attributes == null) {
			return null;
		}		
		
		AWorkspaceNode node = getNode(attributes);
		
		if (node == null) { 
			return null;
		}
		String id = node.getId();

		final Path path = new Path(parent == null ? null : parent.toString());
		path.setName(id);
		if (!tree.contains(path.path)) {
			tree.addElement(path.parentPath == null ? tree : path.parentPath, node, path.path, IndexedTree.AS_CHILD);
		}
		return path;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
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