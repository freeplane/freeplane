package org.freeplane.plugin.bugreport;

import org.freeplane.core.resources.ResourceController;

class ReportRegistry {
	private static final String BUGREPORT = "org.freeplane.plugin.bugreport.";
	private static final ReportRegistry instance = new ReportRegistry();

	static ReportRegistry getInstance() {
		return instance;
	}

	synchronized boolean isReportRegistered(final String hash) {
		return null != ResourceController.getResourceController().getProperty(BUGREPORT + hash,
		    null);
	}

	synchronized void registerReport(final String hash, final String lastReportInfo) {
		final ResourceController resourceController = ResourceController.getResourceController();
		resourceController.setProperty(BUGREPORT + hash, "1");
		ResourceController.getResourceController().setProperty(ReportGenerator.LAST_BUG_REPORT_INFO, lastReportInfo);
	}

	synchronized void unregisterReport(final String hash) {
		ResourceController.getResourceController().getProperties().remove(BUGREPORT + hash);
	}
}
