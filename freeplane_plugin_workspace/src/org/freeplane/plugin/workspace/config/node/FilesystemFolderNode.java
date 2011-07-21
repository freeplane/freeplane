package org.freeplane.plugin.workspace.config.node;

import java.io.File;
import java.net.URL;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.plugin.workspace.WorkspaceEnvironment;

public class FilesystemFolderNode extends WorkspaceNode implements TreeExpansionListener{

	private URL folderPath;
	private boolean isUpToDate = false;
	
	public FilesystemFolderNode(String id) {
		super(id);
	}

	public String toString() {
		return this.getName();
	}

	public URL getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(URL folderPath) {
		this.folderPath = folderPath;
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
				folderNode.add(new DefaultMutableTreeNode(new String(file.getName())));
			}
		}
		if(parent != folderNode)
			parent.add(folderNode);
		
	}
	
	
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
	}
	
	public void treeExpanded(TreeExpansionEvent event) {
		if(isUpToDate||getFolderPath()==null) return;
		File folder = new File(getFolderPath().getFile());
		if(folder.isDirectory()) {			
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
			node.removeAllChildren();
			interateFolder(folder, node);
			WorkspaceEnvironment.getCurrentWorkspaceEnvironment().getViewModel().reload(node);
			isUpToDate = true;
		}
		
	}
	
	
}
