package org.docear.plugin.search;

import java.util.Collection;

import org.docear.plugin.core.DocearService;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.osgi.framework.BundleContext;

public class Activator extends DocearService {

	DocearSearchController controller;
	public void stop(BundleContext bundleContext) throws Exception {
	}

	public void startService(BundleContext context, ModeController modeController) {
		// instantiate first controller (entry controller)
		controller = DocearSearchController.getController();
	}

	protected Collection<IControllerExtensionProvider> getControllerExtensions() {
		return null;
	}

}
