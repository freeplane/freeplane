package org.freeplane.plugin.bugreport;

import org.freeplane.core.resources.ResourceController;

class ReportRegistry {
	private static final ReportRegistry instance = new ReportRegistry();

	static ReportRegistry getInstance() {
		return instance;
	}

	boolean isReportRegistered(final String hash) {
		return null != ResourceController.getResourceController().getProperty("org.freeplane.plugin.bugreport." + hash,
		    null);
	}

	void registerReport(final String hash) {
		ResourceController.getResourceController().setProperty("org.freeplane.plugin.bugreport." + hash, "1");
	}

	void unregisterReport(final String hash) {
		ResourceController.getResourceController().getProperties().remove("org.freeplane.plugin.bugreport." + hash);
	}
}
