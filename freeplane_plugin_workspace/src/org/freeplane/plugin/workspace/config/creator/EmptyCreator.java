package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

public class EmptyCreator extends AConfigurationNodeCreator {
	
	public EmptyCreator() {
	}

	public AWorkspaceNode getNode(final String name, final XMLElement data) {
		return null;
	}
}
