package org.freeplane.plugin.bugreport;

import java.util.Map;

public interface IBugReportListener {
	void onReportSent(Map<String, String> report, String status);
}
