/**
 * author: Marcel Genzmehr
 * 21.07.2011
 */
package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.util.Vector;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public abstract class AFileNodeCreator implements IFileTypeHandler {
	abstract public AWorkspaceTreeNode getNode(String name, File file);
	private final Vector<Object> typeList = new Vector<Object>();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public AFileNodeCreator() {
	}
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void addFileType(final String type) {
		assert(type != null);
		if(!typeList.contains(type)) {
			LogUtils.info("["+this.getClass().getName()+"] addFileType: "+type);
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
	public AWorkspaceTreeNode createFileNode(AWorkspaceTreeNode parent, String fileExtension, final File file) {		
		final AWorkspaceTreeNode node = getNode(file.getName(), file);
		if (node != null) {
			parent.getModel().addNodeTo(node, parent, false);
			return node;
		}
		return parent;		
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
