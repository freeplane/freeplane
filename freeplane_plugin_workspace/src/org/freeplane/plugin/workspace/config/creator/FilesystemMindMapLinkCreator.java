package org.freeplane.plugin.workspace.config.creator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FilesystemMindMapLinkNode;

public class FilesystemMindMapLinkCreator extends AConfigurationNodeCreator {

	public FilesystemMindMapLinkCreator() {
	}

	@Override
	public AWorkspaceNode getNode(String id, XMLElement data) {
		FilesystemMindMapLinkNode node = new FilesystemMindMapLinkNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name == null ? "reference" : name);

		String path = data.getAttribute("path", null);
		if (path != null) {
			LogUtils.info("FilesystemLinkPath: " + path);
			File f = new File(path);
			if (!f.isAbsolute()) {
				path = WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation() + File.separator + path;				
			}
			URL url = null;
			try {
				url = new URL("file://" + path);
				LogUtils.info("FilesystemFolderCreator.getNode: " + url);
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			node.setLinkPath(url);
		}
		return node;

	}

	

}
