package org.freeplane.plugin.workspace.config.creator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FilesystemFolderNode;

public class FilesystemFolderCreator extends AConfigurationNodeCreator {

	public FilesystemFolderCreator() {
	}

	@Override
	public AWorkspaceNode getNode(String id, XMLElement data) {
		FilesystemFolderNode node = new FilesystemFolderNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name == null ? "folder" : name);
		String path = data.getAttribute("path", null);

		if (path != null) {
			LogUtils.info("FilesystemPath: " + path);
			URI uri;
			File f;
			try {
				uri = new URI(path);
				f = new File (UrlManager.getController().getAbsoluteUrl(WorkspaceUtils.getWorkspaceBaseUrl().toURI(), uri).getFile());
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
				return node;				
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
				return node;
			}
						
			if (!f.exists()) {
				if (f.mkdirs()) {
					LogUtils.info("New Filesystem Folder Created: " + f.getAbsolutePath());
				}
			}
			
			node.setFolderPath(uri);

		}
		return node;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		final IndexedTree tree = WorkspaceController.getCurrentWorkspaceController().getTree();
		super.endElement(parent, tag, userObject, lastBuiltElement);
		final Path path = (Path) userObject;
		if (path.path == null) {
			return;
		}
		final DefaultMutableTreeNode treeNode = tree.get(path.path);
		LogUtils.info("debug treeNode.getChildCount: " + treeNode.getChildCount());
		if (treeNode.getChildCount() == 0) {
			treeNode.add(new DefaultMutableTreeNode(new Boolean(true)));
		}

	}

}
