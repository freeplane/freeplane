package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.ListHashTable;

public class FilesystemReader {
		
	private final FileReadManager typeManager;
	
	public FilesystemReader(final FileReadManager typeManager) {
		this.typeManager = typeManager;
	}
	
	private ListHashTable<String, IFileTypeHandler> getFileTypeHandlers() {
		return typeManager.getFileTypeHandlers();
	}
		
	private void iterateFolder(File folder, DefaultMutableTreeNode parent) {
		iterateFolder(folder, parent, true);
	}
	
	private void iterateFolder(File folder, DefaultMutableTreeNode parent, boolean first) {
		DefaultMutableTreeNode folderNode;
		if(first)
			folderNode = parent;
		else
			folderNode = new DefaultMutableTreeNode(new String(folder.getName()));
		
		for(File file : folder.listFiles()) {
			if(file.isDirectory()) {
				iterateFolder(file, folderNode, false);
			}
			else {
				folderNode.add(createFileNode(join(parent.getPath()), file));
			}
		}
		if(parent != folderNode)
			parent.add(folderNode);
		
	}
	
	public void scanFilesystem(final DefaultMutableTreeNode node, final File file) {
		
		if(file != null && file.exists()) {
			if(file.isDirectory()) {		
				iterateFolder(file, node);
			} 
			else {
				node.add(createFileNode(join(node.getPath()), file));
			}
		}
	}
	
	private DefaultMutableTreeNode createFileNode(final Object parent, final File file) {
		ListHashTable<String, IFileTypeHandler> creators = getFileTypeHandlers();
		IFileTypeHandler creator = creators.list("*").get(0);
		//return new DefaultMutableTreeNode(creator.createFileNode(parent, "*", file));
		return new DefaultMutableTreeNode(new String(file.getName()));
	}
	
	private String join(Object[] arry) {
		String joined = "";
		for(Object o : arry) {
			joined += "/"+o.toString();
		}
		return joined;
	}
	
}
