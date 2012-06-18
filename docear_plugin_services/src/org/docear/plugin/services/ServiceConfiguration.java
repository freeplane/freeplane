package org.docear.plugin.services;

import org.docear.plugin.core.ALanguageController;
import org.docear.plugin.services.recommendations.RecommendationDownloadObserver;
import org.freeplane.features.mode.ModeController;

public class ServiceConfiguration extends ALanguageController {
	
	public ServiceConfiguration(ModeController modeController) {
		super();
		RecommendationDownloadObserver.install();
	}

	
}
