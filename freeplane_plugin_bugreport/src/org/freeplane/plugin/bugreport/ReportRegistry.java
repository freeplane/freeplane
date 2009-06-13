package org.freeplane.plugin.bugreport;

import org.freeplane.core.resources.ResourceController;

class ReportRegistry {
	void registerReport(String hash){
		ResourceController.getResourceController().setProperty("org.freeplane.plugin.bugreport." + hash, "1");
	}

	boolean isReportRegistered(String hash){
		return null != ResourceController.getResourceController().getProperty("org.freeplane.plugin.bugreport." + hash, null);
	}

}
