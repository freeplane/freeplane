package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.LocationDialog;
import org.freeplane.plugin.workspace.model.action.AWorkspaceAction;

/**
 * 
 */
public class WorkspaceChangeLocationsAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1544504104584534059L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public WorkspaceChangeLocationsAction() {
		super("workspace.action.docear.locations.change");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void actionPerformed(ActionEvent e) {
		LocationDialog dialog = new LocationDialog();
		
    	dialog.setVisible(true);
	}
}
