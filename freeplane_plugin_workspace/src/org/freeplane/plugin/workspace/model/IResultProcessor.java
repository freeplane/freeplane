package org.freeplane.plugin.workspace.model;

import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public interface IResultProcessor {
	
	void process(AWorkspaceTreeNode parent, AWorkspaceTreeNode node);
	void setProject(AWorkspaceProject project);

}
