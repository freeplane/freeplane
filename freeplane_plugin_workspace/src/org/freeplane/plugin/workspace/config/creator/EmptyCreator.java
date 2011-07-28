package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.WorkspaceNode;

public class EmptyCreator extends ConfigurationNodeCreator {
	
	public EmptyCreator() {
	}

	public WorkspaceNode getNode(final String name, final XMLElement data) {
		return null;
	}
}
