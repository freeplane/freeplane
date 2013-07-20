package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@CheckEnableOnPopup
public class WorkspaceProjectOpenLocationAction extends NodeOpenLocationAction {
	private static final long serialVersionUID = 1L;

	public WorkspaceProjectOpenLocationAction() {
		super();
	}

	@Override
	public void setEnabled() {
		if(WorkspaceController.getCurrentProject() == null) {
			setEnabled(false);
		}
		else {
			setEnabled(true);
		}	
	}

	public void actionPerformed(ActionEvent event) {
		AWorkspaceProject project = WorkspaceController.getCurrentProject();
		if(project == null) {
			return;
		}
	
		openFolder(URIUtils.getAbsoluteFile(project.getProjectHome()));
		
	}
}
