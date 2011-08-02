package org.freeplane.plugin.workspace.config.creator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FilesystemFolderNode;

public class FilesystemFolderCreator extends AConfigurationNodeCreator {

	public FilesystemFolderCreator() {
	}

	@Override
	public AWorkspaceNode getNode(String id, XMLElement data) {
		FilesystemFolderNode node = new FilesystemFolderNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "folder" : name);
		String path = data.getAttribute("path", null);
				
		if(path!=null) {
			LogUtils.info("FilesystemPath: "+path);
			File f = new File(path);
			if (!f.isAbsolute()) {
				path = WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation()+File.separator+path;
				f = new File(path);
				if (!f.exists()) {
					if (f.mkdir()) {
						LogUtils.info("New Filesystem Folder Created: "+f.getAbsolutePath());
					}
				}
			}
			URL url = null;
			try {
				url = new URL("file://"+path);
				LogUtils.info("FilesystemFolderCreator.getNode: "+url);
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			node.setFolderPath(url);
		}
		return node;
	}
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		final IndexedTree tree = WorkspaceController.getCurrentWorkspaceController().getTree();
		super.endElement(parent, tag, userObject, lastBuiltElement);
		final Path path = (Path)userObject;
		if (path.path == null) {
			return;
		}
		final DefaultMutableTreeNode treeNode = tree.get(path.path);
		LogUtils.info("debug treeNode.getChildCount: "+treeNode.getChildCount());
		if(treeNode.getChildCount() == 0) {
			treeNode.add(new DefaultMutableTreeNode(new Boolean(true)));
		}
		
	}

}
