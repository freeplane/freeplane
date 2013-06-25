package org.freeplane.plugin.workspace.model.project;

import java.net.URI;

public interface IWorkspaceProjectCreater {
	AWorkspaceProject newProject(String projectID, URI projectHome);
}
