package org.freeplane.plugin.workspace.model.project;

import java.util.EventListener;

import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;


public interface IProjectModelListener extends EventListener {

	public void treeNodesChanged(WorkspaceModelEvent event);

	public void treeNodesInserted(WorkspaceModelEvent event);

	public void treeNodesRemoved(WorkspaceModelEvent event);

	public void treeStructureChanged(WorkspaceModelEvent event);
	
}
