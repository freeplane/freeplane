/**
 * author: Marcel Genzmehr
 * 19.08.2011
 */
package org.docear.plugin.core;

import java.util.Vector;

import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.util.LogUtils;

/**
 * 
 */
public class DocearController implements IDocearEventListener, IFreeplanePropertyListener {
//	public final static String DOCUMENT_REPOSITORY_PATH_PROPERTY = "document_repository_path";
//	public final static String PROJECTS_PATH_PROPERTY = "docear_projects_path";
//	public final static String BIBTEX_PATH_PROPERTY = "docear_bibtex_path";
	
	private final Vector<IDocearEventListener> docearListeners = new Vector<IDocearEventListener>();		
	private final static DocearController docearController = new DocearController();
	
	private IDocearLibrary currentLibrary = null;
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	protected DocearController() {
		addDocearEventListener(this);
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static DocearController getController() {
		return docearController;
	}
		
	public void addDocearEventListener(IDocearEventListener listener) {
		if(this.docearListeners.contains(listener)) {
			return;
		}
		this.docearListeners.add(listener);
	}
	
	public void removeDocearEventListener(IDocearEventListener listener) {
		this.docearListeners.remove(listener);
	}
	
	public void removeAllDocearEventListeners() {
		this.docearListeners.removeAllElements();
	}
	
	public void dispatchDocearEvent(DocearEvent event) {
		LogUtils.info("DOCEAR: dispatchEvent: "+ event);
		for(IDocearEventListener listener : this.docearListeners) {
			listener.handleEvent(event);
		}
	}
	
	public IDocearLibrary getLibrary() {
		return currentLibrary;
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.NEW_LIBRARY && event.getSource() instanceof IDocearLibrary) {
			this.currentLibrary = (IDocearLibrary) event.getSource();
			LogUtils.info("DOCEAR: new DocearLibrary set");
		}	
	}

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		// TODO Auto-generated method stub
	}
}
