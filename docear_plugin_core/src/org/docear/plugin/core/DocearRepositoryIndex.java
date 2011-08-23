/**
 * author: Marcel Genzmehr
 * 22.08.2011
 */
package org.docear.plugin.core;

import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.IDocearEventListener;

/**
 * 
 */
public class DocearRepositoryIndex implements IDocearEventListener {
	
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearRepositoryIndex() {
		DocearController.getController().addDocearEventListener(this);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(DocearEvent event) {
		

	}
}
