package org.docear.plugin.communications;

import java.net.URL;


import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;

public class CommunicationsConfiguration {
	public CommunicationsConfiguration() {
		addLanguageResources();
		addPluginDefaults();
	}
	
	private void addLanguageResources() {
		ResourceBundles resBundle = ((ResourceBundles)Controller.getCurrentModeController().getController().getResourceController().getResources());
		String lang = resBundle.getLanguageCode();
		if (lang == null || lang.equals(ResourceBundles.LANGUAGE_AUTOMATIC)) {
			lang = "en";
		}
		
		final URL res = this.getClass().getResource("/translations/Resources_"+resBundle.getLanguageCode()+".properties");
		System.out.println("DOCEAR res: "+res);
		//resBundle.addResources(resBundle.getLanguageCode(), res);
	}
	
	private void addPluginDefaults() {
//		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
//		if (defaults == null)
//			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
//		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}
}
