package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;

public class WorkspaceRoot extends AWorkspaceNode implements IWorkspaceNodeEventListener {

	public WorkspaceRoot(String id) {
		super(id);
	}

	public String getTagName() {
		return "workspace_structure";
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			WorkspaceController.getCurrentWorkspaceController().getPopups()
					.showWorkspacePopup((Component) event.getSource(), event.getX(), event.getY());

		}
	}
}
