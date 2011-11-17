package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;

import org.freeplane.plugin.workspace.WorkspaceChooserDialog;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;

public class WorkspaceChangeLocationAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceChangeLocationAction() {
		super("workspace.action.location.change");
	}

	public void actionPerformed(final ActionEvent e) {
		System.out.println("WorkspaceSetLocationAction: " + e.getActionCommand() + " : " + e.getID());
		showLocationSwitcherDialog();		
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
