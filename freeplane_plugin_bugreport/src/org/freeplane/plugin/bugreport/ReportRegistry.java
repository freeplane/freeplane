package org.freeplane.plugin.bugreport;

import org.freeplane.core.resources.ResourceController;

class ReportRegistry {
	boolean isReportRegistered(final String hash) {
		return null != ResourceController.getResourceController().getProperty("org.freeplane.plugin.bugreport." + hash,
		    null);
	}

	void registerReport(final String hash) {
		ResourceController.getResourceController().setProperty("org.freeplane.plugin.bugreport." + hash, "1");
	}
}
