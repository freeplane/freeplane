package org.docear.plugin.core;

import java.net.URL;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class CoreConfiguration {
	
	
	public CoreConfiguration(ModeController modeController) {
		LogUtils.info("org.docear.plugin.core.CoreConfiguration() initializing...");
		init(modeController);
	}

	private void init(ModeController modeController) {
		addPluginDefaults();		
	}
	
	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}
	
	
}


