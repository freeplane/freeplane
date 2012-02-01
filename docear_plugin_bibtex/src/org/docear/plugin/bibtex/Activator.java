package org.docear.plugin.bibtex;

import org.docear.plugin.core.DocearService;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleContext;

public class Activator extends DocearService {

	public void startService(BundleContext context, ModeController modeController) {
		new ReferencesController(modeController);
	}

	public void stop(BundleContext context) throws Exception {
		
	}

	
}
