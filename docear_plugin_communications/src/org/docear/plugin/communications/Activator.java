package org.docear.plugin.communications;

import java.util.Collection;

import org.docear.plugin.core.DocearService;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.osgi.framework.BundleContext;

public class Activator extends DocearService {


	public void stop(BundleContext context) throws Exception {
	}

	public void startService(BundleContext context, ModeController modeController) {
		CommunicationsController.initialize(modeController);
	}

	protected Collection<IControllerExtensionProvider> getControllerExtensions() {
		return null;
	}

}
