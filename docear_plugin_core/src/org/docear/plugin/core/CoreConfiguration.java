package org.docear.plugin.core;

import java.net.URL;
import java.util.Enumeration;

import org.docear.plugin.core.actions.DocearLicenseAction;
import org.docear.plugin.core.actions.DocearOpenUrlAction;
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
		replaceFreeplaneStringsAndActions();		
	}
	
	private void replaceFreeplaneStringsAndActions() {
		ResourceController resourceController = ResourceController.getResourceController(); 
				
		if(!resourceController.getProperty("ApplicationName", "").equals("Docear")){
			return;
		}
		
		replaceResourceBundleStrings();
		
		replaceActions();	
	}

	private void replaceActions() {
		ResourceController resourceController = ResourceController.getResourceController(); 
		
		resourceController.setProperty("webFreeplaneLocation", resourceController.getProperty("webDocearLocation"));	
		replaceAction("OpenFreeplaneSiteAction", new DocearOpenUrlAction("OpenFreeplaneSiteAction",  resourceController.getProperty("webFreeplaneLocation")));
		resourceController.setProperty("bugTrackerLocation", resourceController.getProperty("docear_bugTrackerLocation"));	
		replaceAction("ReportBugAction", new DocearOpenUrlAction("ReportBugAction",  resourceController.getProperty("bugTrackerLocation")));
		resourceController.setProperty("helpForumLocation", resourceController.getProperty("docear_helpForumLocation"));	
		replaceAction("AskForHelp", new DocearOpenUrlAction("AskForHelp",  resourceController.getProperty("helpForumLocation")));
		resourceController.setProperty("featureTrackerLocation", resourceController.getProperty("docear_featureTrackerLocation"));	
		replaceAction("RequestFeatureAction", new DocearOpenUrlAction("RequestFeatureAction",  resourceController.getProperty("featureTrackerLocation")));
		resourceController.setProperty("webDocuLocation", resourceController.getProperty("docear_webDocuLocation"));	
		replaceAction("DocumentationAction", new DocearOpenUrlAction("DocumentationAction",  resourceController.getProperty("webDocuLocation")));
		replaceAction("LicenseAction", new DocearLicenseAction("LicenseAction"));
	}

	private void replaceResourceBundleStrings() {
		ResourceController resourceController = ResourceController.getResourceController(); 
		ResourceBundles bundles = ((ResourceBundles)resourceController.getResources());
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
		
		bundles.putResourceString("about_text", "Docear About Text.\nDocear Version: 1.0 Alpha \nFreeplane Version: ");
	}

	private void replaceAction(String actionKey, AFreeplaneAction action) {		
		Controller controller = Controller.getCurrentController();
		
		controller.removeAction(actionKey);
		controller.addAction(action);
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}
	
	
}


