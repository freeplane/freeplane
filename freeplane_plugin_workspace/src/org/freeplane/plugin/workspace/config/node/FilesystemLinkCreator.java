package org.freeplane.plugin.workspace.config.node;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLElement;

public class FilesystemLinkCreator extends NodeCreator {

	public FilesystemLinkCreator(IndexedTree tree) {
		super(tree);
	}

	@Override
	public ConfigurationNode getNode(String id, XMLElement data) {
		FilesystemLinkNode node = new FilesystemLinkNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name==null? "reference" : name);
		return node;
	}

}
