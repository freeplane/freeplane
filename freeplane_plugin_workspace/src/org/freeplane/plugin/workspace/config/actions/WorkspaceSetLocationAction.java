package org.freeplane.plugin.workspace.config.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;

import javax.swing.JFileChooser;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
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
		showLocationSwitcherDialog();		
	}

	private void showLocationSwitcherDialog() {
		JFileChooser fileChooser = new JFileChooser();		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		ResourceController resourceController = ResourceController.getResourceController();
		String currentLocation = resourceController.getProperty(WorkspacePreferences.WORKSPACE_LOCATION);
		
		if (currentLocation != null) {
			File file = new File(currentLocation);
			if (file.exists()) {
				fileChooser.setSelectedFile(file);
			}
		}

		int retVal = fileChooser.showOpenDialog(UITools.getFrame());
		if (retVal == JFileChooser.APPROVE_OPTION) {
			
			File selectedfile = fileChooser.getSelectedFile();
			ResourceController.getResourceController().setProperty(WorkspacePreferences.WORKSPACE_LOCATION_NEW,
					selectedfile.getPath());
			Controller.getCurrentController().getResourceController()
					.setProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY, true);
			WorkspaceController.getCurrentWorkspaceController().refreshWorkspace();
			
		}

	}

}
