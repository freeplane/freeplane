package org.freeplane.plugin.workspace.config.creator;

import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
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
			
			URI uri;
			try {
				uri = new URI(path);
				node.setLinkPath(uri);
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			} 
		}
		return node;

	}

	

}
