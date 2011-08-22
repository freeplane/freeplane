/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.docear.plugin.core;

import java.util.Vector;

/**
 * 
 */
public class DocearController {
	
	private final static DocearController docearController = new DocearController();
	private final Vector<IDocearEventListener> docearListener = new Vector<IDocearEventListener>();
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	protected DocearController() {
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static DocearController getController() {
		return docearController;
	}
	
	public void addDocearEventListener(IDocearEventListener listener) {
		if(this.docearListener.contains(listener)) {
			return;
		}
		this.docearListener.add(listener);
	}
	
	public void removeDocearEventListener(IDocearEventListener listener) {
		this.docearListener.remove(listener);
	}
	
	public void removeAllDocearEventListeners() {
		this.docearListener.removeAllElements();
	}
	
	public void dispatchDocearEvent(DocearEvent event) {
		for(IDocearEventListener listener : this.docearListener) {
			listener.handleEvent(event);
		}
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
