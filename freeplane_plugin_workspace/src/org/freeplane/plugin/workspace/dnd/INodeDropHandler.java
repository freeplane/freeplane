package org.freeplane.plugin.workspace.dnd;

import java.awt.datatransfer.Transferable;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public interface INodeDropHandler {

	public boolean processDrop(AWorkspaceTreeNode targetNode, Transferable transferable, int dropAction);

	public boolean acceptDrop(Transferable transferable);

}
