/**
 * author: Marcel Genzmehr
 * 02.12.2011
 */
package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.QuitAction;

/**
 * 
 */
public class DocearQuitAction extends QuitAction {

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
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.APPLICATION_CLOSED);
		LogUtils.info("saving all docear components ...");
	}
}
