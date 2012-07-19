package org.docear.plugin.core.logging;

import org.freeplane.core.util.LogUtils;

public class DocearLogger {

	public static void info(String msg) {
		LogUtils.info(msg);
	}
	
	public static void warn(String msg) {
		LogUtils.warn(msg);
	}
	
	public static void error(String msg) {
		LogUtils.severe(msg);
	}

}
