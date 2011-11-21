/**
 * author: Marcel Genzmehr
 * 21.11.2011
 */
package org.freeplane.plugin.workspace.view;

import java.awt.event.ActionEvent;

import org.freeplane.features.mode.QuitAction;
import org.freeplane.plugin.workspace.WorkspaceUtils;


public class WorkspaceInformingQuitAction extends QuitAction {

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
		System.out.println("DOCEAR: save config ...");
		WorkspaceUtils.saveCurrentConfiguration();
		super.actionPerformed(e);
	}
}
