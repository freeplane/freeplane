/**
 * author: Marcel Genzmehr
 * 02.12.2011
 */
package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.QuitAction;

/**
 * 
 */
public class ReferenceQuitAction extends QuitAction {
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
		LogUtils.info("saving all references ...");
		ReferencesController.getController().getJabrefWrapper().getBasePanel().runCommand("save");
	}
}
