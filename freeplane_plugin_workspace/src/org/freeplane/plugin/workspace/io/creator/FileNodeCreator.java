/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;
import org.freeplane.plugin.workspace.io.IFileTypeHandler;

public abstract class FileNodeCreator implements IFileTypeHandler {
	abstract public WorkspaceNode getNode(String name, File file);
	
	
	protected IndexedTree tree;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FileNodeCreator(IndexedTree tree) {
		this.tree = tree;
	}
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	/* (non-Javadoc)
	 * @see org.freeplane.plugin.workspace.io.IFileHandler#createFileNode(java.lang.Object, java.lang.String, java.io.File)
	 */
	public Object createFileNode(Object parent, String fileExtension, File file) {
		final Path path = new Path(parent == null ? null : parent.toString());		
		path.setName(file.getName());		
		if (!tree.contains(path.path)) {
			final DefaultMutableTreeNode treeNode =	tree.addElement(path.parentPath == null ? tree : path.parentPath, this, path.path, IndexedTree.AS_CHILD);			
			if (treeNode.getUserObject() == this) {
				final WorkspaceNode node = getNode(file.getName(), file);
				if(node != null) 
					treeNode.setUserObject(node);
				else 
					tree.removeElement(path.path);
			}
		}		
		return path;		
	}

	/***********************************************************************************
	 * INTERNAL CLASSES
	 **********************************************************************************/
	
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
	}
}
