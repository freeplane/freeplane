package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.dialog.NewProjectDialogPanel;
import org.freeplane.plugin.workspace.io.IProjectSettingsIOHandler.LOAD_RETURN_TYPE;
import org.freeplane.plugin.workspace.model.WorkspaceModelException;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceNewProjectAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "workspace.action.project.new";
	
	public WorkspaceNewProjectAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		NewProjectDialogPanel dialog = new NewProjectDialogPanel();
		int response = JOptionPane.showConfirmDialog(UITools.getFrame(), dialog, TextUtils.getText("workspace.action.node.new.project.dialog.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(response == JOptionPane.OK_OPTION) {
			File path = URIUtils.getFile(dialog.getProjectPath());
			
			//WORKSPACE - todo: ask for permission to create the directory or check for always_create setting
			if(!path.exists() ) {
				path.mkdir();
			}
					
			AWorkspaceProject project = AWorkspaceProject.create(null, path.toURI());
			WorkspaceController.getCurrentModel().addProject(project);
			try {
				LOAD_RETURN_TYPE return_type = WorkspaceController.getCurrentModeExtension().getProjectLoader().loadProject(project);
				if(return_type == LOAD_RETURN_TYPE.NEW_PROJECT && dialog.getProjectName() != null && dialog.getProjectName().length() > 0) {
					project.getModel().changeNodeName(project.getModel().getRoot(), dialog.getProjectName());
				}
			} catch (IOException e) {
				LogUtils.severe(e);
			} catch (WorkspaceModelException e) {
				LogUtils.severe(e);
			}
		}
	}

}
