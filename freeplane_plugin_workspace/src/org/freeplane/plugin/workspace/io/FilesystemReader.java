package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IElementContentHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ListHashTable;
import org.freeplane.core.io.ReadManager;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceEnvironment;

public class FilesystemReader {
	
	private Object currentElement;
	final private LinkedList<Object> elementStack = new LinkedList<Object>();
	private IFileTypeHandler nodeCreator;
	final private LinkedList<IFileTypeHandler> nodeCreatorStack = new LinkedList<IFileTypeHandler>();
	private Object parentElement;
	
	private final FileReadManager typeManager;
	
	public FilesystemReader(final FileReadManager typeManager) {
		this.typeManager = typeManager;
	}
	
	private ListHashTable<String, IFileTypeHandler> getFileTypeHandlers() {
		return typeManager.getFileTypeHandlers();
	}
		
	private void interateFolder(File folder, DefaultMutableTreeNode parent) {
		interateFolder(folder, parent, true);
	}
	
	private void interateFolder(File folder, DefaultMutableTreeNode parent, boolean first) {
		DefaultMutableTreeNode folderNode;
		if(first)
			folderNode = parent;
		else
			folderNode = new DefaultMutableTreeNode(new String(folder.getName()));
		
		for(File file : folder.listFiles()) {
			if(file.isDirectory()) {
				interateFolder(file, folderNode, false);
			}
			else {
				folderNode.add(createFileNode(file));
			}
		}
		if(parent != folderNode)
			parent.add(folderNode);
		
	}
	
	public void scanFilesystem(final DefaultMutableTreeNode node, final File file) {
		
		if(file != null && file.exists()) {
			if(file.isDirectory()) {		
				interateFolder(file, node);
			} 
			else {
				node.add(createFileNode(file));
			}
		}
	}
	
	private DefaultMutableTreeNode createFileNode(File file) {
		return new DefaultMutableTreeNode(new String(file.getName()));
	}
	
}
