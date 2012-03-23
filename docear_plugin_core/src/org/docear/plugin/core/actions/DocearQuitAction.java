/**
 * author: Marcel Genzmehr
 * 02.12.2011
 */
package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.QuitAction;

/**
 * 
 */
public class DocearQuitAction extends QuitAction {

	private static final long serialVersionUID = 1L;
	private final AFreeplaneAction previousAction; 
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearQuitAction(AFreeplaneAction action) {
		this.previousAction = action;
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void actionPerformed(ActionEvent e) {
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.APPLICATION_CLOSED);
		LogUtils.info("saving all docear components ...");
		if(Controller.getCurrentController().getViewController().quit()) {
			while(DocearController.getController().hasWorkingThreads()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
			this.previousAction.actionPerformed(e);
		}
	}
}
