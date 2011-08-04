package org.docear.plugin.core;

import java.net.URL;
import java.util.Enumeration;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class CoreConfiguration {
	
	
	public CoreConfiguration(ModeController modeController) {
		LogUtils.info("org.docear.plugin.core.CoreConfiguration() initializing...");
		init(modeController);
	}

	private void init(ModeController modeController) {
		addPluginDefaults();
		addDocearProperties();		
	}
	
	private void addDocearProperties() {
		ResourceBundles bundles = ((ResourceBundles)ResourceController.getResourceController().getResources());
		Controller controller = Controller.getCurrentController();
		for (Enumeration<?> i = bundles.getKeys(); i.hasMoreElements();){
			String key = i.nextElement().toString();
			String value = bundles.getResourceString(key);
			if(value.matches(".*[Ff][Rr][Ee][Ee][Pp][Ll][Aa][Nn][Ee].*")){
				value = value.replaceAll("[Ff][Rr][Ee][Ee][Pp][Ll][Aa][Nn][Ee]", "Docear");
				bundles.putResourceString(key, value);
				if(key.matches(".*[.text]")){
					key = key.replace(".text", "");
					AFreeplaneAction action = controller.getAction(key);
					if(action != null){
						MenuBuilder.setLabelAndMnemonic(action, value);
					}
				}				
			}			
		}
		
		
		//.putResourceString("mode_title", "Docear - {0}");
		//((ResourceBundles)ResourceController.getResourceController().getResources()).putResourceString("OpenFreeplaneSiteAction.text", "Docear &Webseite");
		
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}
	
	
}


