package org.docear.plugin.bibtex;

import org.docear.plugin.core.DocearPlugin;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleContext;

public class Activator extends DocearPlugin {

	public void startPlugin(BundleContext context, ModeController modeController) {
		new ReferencesController(modeController);
	}

	public void stop(BundleContext context) throws Exception {
		
	}

	
}
