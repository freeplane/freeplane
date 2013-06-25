/**
 * author: Marcel Genzmehr
 * 15.11.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange = true)
public class FileNodeNewFileAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FileNodeNewFileAction() {
		super("workspace.action.file.new.file");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setEnabled() {
		setEnabled(false);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {
	}
}
