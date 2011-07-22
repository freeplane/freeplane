/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io.creator;

import java.io.File;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;

public abstract class FileNodeCreator implements IFileTypeHandler {
	abstract public WorkspaceNode getNode(String name, File file);
	
	protected IndexedTree tree;
	private final Vector<Object> typeList = new Vector<Object>();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FileNodeCreator(IndexedTree tree) {
		this.tree = tree;
	}
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void addFileType(final String type) {
		assert(type != null);
		if(!typeList.contains(type)) {
			System.out.println("["+this.getClass().getName()+"] addFileType: "+type);
			typeList.add(type);
		}
	}
	
	public void removeFileType(final String type) {
		assert(type != null);
		typeList.remove(type);
	}
	
	public void setFileTypeList(final String separatedTypes, final String separator) {
		assert(separator != null || separatedTypes!=null);
		this.typeList.removeAllElements();
		String[] tokens = separatedTypes.trim().split("\\s*["+separator+"]\\s*");
		for(String token : tokens) {
			addFileType(token);
		}
	}
	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public Object createFileNode(Object object, String fileExtension,final File file) {
		Object parent = object;
		if(object instanceof WorkspaceNode) {
			parent = tree.getKeyByUserObject(object);
		}
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
		else {
			
		}
		return path;		
	}
	

	public Object[] getSupportedFileTypes() {
		return typeList.toArray();		
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
