package org.docear.plugin.pdfutilities.listener;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.event.IWorkspaceEventListener;
import org.freeplane.plugin.workspace.event.WorkspaceEvent;

public class DefaultWorkspaceEventListener implements IWorkspaceEventListener {

	public void openWorkspace(WorkspaceEvent event) {
	}

	public void closeWorkspace(WorkspaceEvent event) {
	}

	public void workspaceReady(WorkspaceEvent event) {
		WorkspaceController.getController().getWorkspaceModel().addTreeModelListener(new DocearWorkspaceTreeModelListener());
	}

	public void workspaceChanged(WorkspaceEvent event) {
	}

	public void toolBarChanged(WorkspaceEvent event) {
	}

	public void configurationLoaded(WorkspaceEvent event) {
	}

	public void configurationBeforeLoading(WorkspaceEvent event) {
	}

}
