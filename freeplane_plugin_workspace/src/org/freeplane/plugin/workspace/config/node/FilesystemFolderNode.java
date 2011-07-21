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
	
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
	}
	
	public void treeExpanded(TreeExpansionEvent event) {
		if(isUpToDate||getFolderPath()==null) return;
		File folder = new File(getFolderPath().getFile());
		if(folder.isDirectory()) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
			node.removeAllChildren();
			WorkspaceEnvironment.getCurrentWorkspaceEnvironment().getFilesystemReader().scanFilesystem(node, folder);
			WorkspaceEnvironment.getCurrentWorkspaceEnvironment().getViewModel().reload(node);
			isUpToDate = true;
		}
		
	}
	
	
}
