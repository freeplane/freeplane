package org.freeplane.plugin.workspace.config.node;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLElement;

public class EmptyCreator extends NodeCreator {
	
	public EmptyCreator(IndexedTree tree) {		
		super(tree);
	}

	public ConfigurationNode getNode(final String name, final XMLElement data) {
		return null;
	}
}
