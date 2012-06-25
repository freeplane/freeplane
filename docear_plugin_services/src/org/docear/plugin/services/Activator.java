package org.docear.plugin.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.docear.plugin.core.DocearService;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsModeController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.osgi.framework.BundleContext;

public class Activator extends DocearService {
		
	
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye World!!");
	}

	public void startService(BundleContext context, ModeController modeController) {
		ServiceController.initialize(modeController);
	}

	protected Collection<IControllerExtensionProvider> getControllerExtensions() {
		List<IControllerExtensionProvider> controllerExtensions = new ArrayList<IControllerExtensionProvider>();
		controllerExtensions.add(new IControllerExtensionProvider() {
			
			public void installExtension(Controller controller) {
				DocearRecommendationsModeController.createController(controller);				
			}
		});
		return controllerExtensions;
	}

}
