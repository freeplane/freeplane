package org.freeplane.plugin.workspace.config.node;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.config.NodeCreator;

public class EmptyCreator extends NodeCreator {
	@Override
	public DefaultMutableTreeNode getNode(final String name, final XMLElement data) {
		return null;
	}
}
