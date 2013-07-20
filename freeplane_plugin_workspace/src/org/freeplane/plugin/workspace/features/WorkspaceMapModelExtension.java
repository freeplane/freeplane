package org.freeplane.plugin.workspace.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceMapModelExtension implements IExtension {
	private AWorkspaceProject project = null;
	
	public AWorkspaceProject getProject() {
		return this.project;
	}
	
	public void setProject(AWorkspaceProject project) {
		this.project = project;
	}

}
