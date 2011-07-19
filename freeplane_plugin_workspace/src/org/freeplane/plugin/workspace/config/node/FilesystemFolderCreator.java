package org.freeplane.plugin.workspace.config.node;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;

public class FilesystemFolderCreator extends NodeCreator {

	public FilesystemFolderCreator(IndexedTree tree) {
		super(tree);
	}

	@Override
	public ConfigurationNode getNode(String id, XMLElement data) {
		FilesystemFolderNode node = new FilesystemFolderNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "folder" : name);
		String path = data.getAttribute("path", null);
		if(path!=null) {
			URL url = null;
			try {
				url = new URL("file://"+path);
			}
			catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(url);
			node.setFolderPath(url);
		}
		return node;
	}
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, userObject, lastBuiltElement);
		final Path path = (Path)userObject;
		if (path.path == null) {
			return;
		}
		final DefaultMutableTreeNode treeNode = tree.get(path.path);
		if(treeNode.getChildCount() == 0) {
			treeNode.add(new DefaultMutableTreeNode(new Boolean(true)));
		}
		
	}

}
