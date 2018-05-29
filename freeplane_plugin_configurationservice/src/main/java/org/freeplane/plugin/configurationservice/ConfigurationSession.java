package org.freeplane.plugin.configurationservice;

import java.io.File;
import java.util.Map;

import org.freeplane.plugin.script.proxy.Proxy.Controller;
import org.freeplane.plugin.script.proxy.ScriptUtils;

public class ConfigurationSession {

	private Controller c; 

	public ConfigurationSession() {
		c = ScriptUtils.c();		
	}

	void start(String mindmapfile) {
		File templateFile = new File(mindmapfile);
		c.newMapFromTemplate(templateFile);
	}

	public void CreateAttributes(String nodeId, Map<String, Object> attributesMap) {
		c.getSelected().getMap().node(nodeId).setAttributes(attributesMap);
	}

	public void update(String nodeId, String attributeName, int attributeValue) {
		c.getSelected().getMap().node(nodeId).getAttributes().set(attributeName, attributeValue);
		
	}
	

}
