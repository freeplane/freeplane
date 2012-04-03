package org.docear.plugin.backup.listeners;

import org.docear.plugin.backup.actions.DocearAllowUploadChooserAction;
import org.docear.plugin.core.DocearController;
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
			DocearAllowUploadChooserAction.showDialog();
		}
	}

	public void toolBarChanged(WorkspaceEvent event) {
	}

	public void configurationLoaded(WorkspaceEvent event) {
	}

	public void configurationBeforeLoading(WorkspaceEvent event) {
	}

}
