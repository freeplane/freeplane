package org.freeplane.plugin.workspace.model;

import javax.swing.event.TreeModelListener;

public interface WorkspaceModelListener extends TreeModelListener {
	
	public void projectAdded(WorkspaceModelEvent event);
	
	public void projectRemoved(WorkspaceModelEvent event);

}
