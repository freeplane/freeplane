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
import org.osgi.framework.Bundle;

/**
 * 
 */
public class DocearController implements IDocearEventListener, IFreeplanePropertyListener {
	
	private final Vector<DocearPlugin> registeredPlugins = new Vector<DocearPlugin>();
	private boolean registeredPluginsDirty = false;
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
	
	public void registerPlugin(DocearBundleInfo bundleInfo, DocearPlugin plugin) {
		if(registeredPlugins.contains(plugin)) {
			return;
		}
		registeredPluginsDirty = true;
		registeredPlugins.add(plugin);
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
	
	private void startPlugins() {
		if(registeredPluginsDirty) {
			checkAndOrderPlugins();
		}
	}
	
	
	private void checkAndOrderPlugins() {
		int pluginsCount = registeredPlugins.size();
		int processedPlugins = 0;
		int currentPosition = 0;
		int insertPosition = 0;
		while(processedPlugins < pluginsCount) {
			DocearPlugin plugin	= registeredPlugins.remove(currentPosition);
			DocearBundleInfo[] dependencies = plugin.getBundleInfo().getRequiredBundles();
			insertPosition = 0;
			int index = -1;
			
			for(DocearBundleInfo info : dependencies) {
				for(int i=0; i < registeredPlugins.size(); i++) {
					if(registeredPlugins.get(i).getBundleInfo().equals(info)) {
						if(index < i) {
							index = i;
						}
						break;
					}
				}
			}
			if(index > -1) {
				insertPosition = index + 1;
				registeredPlugins.add(insertPosition, plugin);
			}
			processedPlugins++;
		}
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.NEW_LIBRARY && event.getSource() instanceof IDocearLibrary) {
			this.currentLibrary = (IDocearLibrary) event.getSource();
			LogUtils.info("DOCEAR: new DocearLibrary set");
		}
		else
		if(event.getType() == DocearEventType.PLUGIN_START_ALLOWED) {
			startPlugins();
		}		
	}

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		// TODO Auto-generated method stub
	}
}
