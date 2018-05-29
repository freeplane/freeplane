package org.freeplane.plugin.configurationservice;

import org.freeplane.core.util.LogUtils;

public class ConfigurationEngine {

	private String serviceurl;

	public ConfigurationEngine(String serviceurl) {
		//Listen on a given port
		//Open a maps
		
		this.serviceurl = serviceurl;
		LogUtils.info(serviceurl);
	}

	public ConfigurationSession newSession() {
		
		return new ConfigurationSession();
	}

}
