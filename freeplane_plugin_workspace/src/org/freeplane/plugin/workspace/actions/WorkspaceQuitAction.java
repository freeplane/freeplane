/**
 * author: Marcel Genzmehr
 * 21.11.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.QuitAction;
import org.freeplane.plugin.workspace.WorkspaceController;


public class WorkspaceQuitAction extends QuitAction {

	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {
		LogUtils.info("Workspace: saving all settings ...");
		WorkspaceController.getController().shutdown(); 
	}
}
