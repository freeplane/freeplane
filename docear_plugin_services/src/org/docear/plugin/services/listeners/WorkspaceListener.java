package org.docear.plugin.services.listeners;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;
import org.freeplane.plugin.workspace.event.IWorkspaceEventListener;
import org.freeplane.plugin.workspace.event.WorkspaceEvent;

public class WorkspaceListener implements IWorkspaceEventListener {

	public void openWorkspace(WorkspaceEvent event) {		
	}

	public void closeWorkspace(WorkspaceEvent event) {
	}

	public void workspaceReady(WorkspaceEvent event) {
	}

	public void workspaceChanged(WorkspaceEvent event) {
		if (DocearController.getController().isDocearNewVersion()) {
			DocearAllowUploadChooserAction.showDialog(false);
		}
	}

	public void toolBarChanged(WorkspaceEvent event) {
	}

	public void configurationLoaded(WorkspaceEvent event) {
	}

	public void configurationBeforeLoading(WorkspaceEvent event) {
	}

}
