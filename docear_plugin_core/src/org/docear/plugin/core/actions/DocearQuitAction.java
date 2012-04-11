/**
 * author: Marcel Genzmehr
 * 02.12.2011
 */
package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.features.mode.QuitAction;

/**
 * 
 */
public class DocearQuitAction extends QuitAction {

	private static final long serialVersionUID = 1L; 
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearQuitAction() {
		super();
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void actionPerformed(ActionEvent e) {		
		quit(this);	
	}
	
	public static void quit(Object src) {
		if (DocearController.getController().shutdown()) {
			DocearController.getController().getDocearEventLogger().appendToLog(src, DocearLogEvent.APPLICATION_CLOSED);
			System.exit(0);
		}
	}
}
