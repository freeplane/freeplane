package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceChooserDialog;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspacePreferences;

public class WorkspaceSetLocationAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceSetLocationAction() {
		super("WorkspaceSetLocationAction");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("WorkspaceSetLocationAction: " + e.getActionCommand() + " : " + e.getID());
		//resetVariables();
		showLocationSwitcherDialog();		
	}

	private void resetVariables() {
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		resCtrl.setProperty(WorkspaceController.DOCUMENT_REPOSITORY_PATH_PROPERTY, "");
		resCtrl.setProperty(WorkspaceController.BIBTEX_PATH_PROPERTY, "");
		resCtrl.setProperty(WorkspaceController.PROJECTS_PATH_PROPERTY, "");
		
	}

	private void showLocationSwitcherDialog() {
//		JFileChooser fileChooser = new JFileChooser();		
//		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		ResourceController resourceController = ResourceController.getResourceController();
//		String currentLocation = resourceController.getProperty(WorkspacePreferences.WORKSPACE_LOCATION);
//		
//		if (currentLocation != null) {
//			File file = new File(currentLocation);
//			if (file.exists()) {
//				fileChooser.setSelectedFile(file);
//			}
//		}
//
//		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
//		if (retVal == JFileChooser.APPROVE_OPTION) {			
//			File selectedfile = fileChooser.getSelectedFile();
//			WorkspaceController.getController().setWorkspaceLocation(selectedfile.getPath());
//		}
//		WorkspaceController workspaceController = WorkspaceController.getController();
//		workspaceController.getConfiguration().setConfigValid(false);
//		workspaceController.reloadWorkspace();
		
		WorkspaceChooserDialog locationDialog = new WorkspaceChooserDialog();
		locationDialog.setVisible(true);
		
		WorkspaceController workspaceController = WorkspaceController.getController();
		workspaceController.reloadWorkspace();
		

	}

}
