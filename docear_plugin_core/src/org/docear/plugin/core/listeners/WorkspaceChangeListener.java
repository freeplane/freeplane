package org.docear.plugin.core.listeners;

import org.docear.plugin.core.CoreConfiguration;
import org.freeplane.plugin.workspace.controller.IWorkspaceListener;
import org.freeplane.plugin.workspace.controller.WorkspaceEvent;

public class WorkspaceChangeListener implements IWorkspaceListener {

	public void workspaceChanged(WorkspaceEvent event) {
		CoreConfiguration.projectPathObserver.reset();
		CoreConfiguration.referencePathObserver.reset();
		CoreConfiguration.repositoryPathObserver.reset();
	}
	

}
