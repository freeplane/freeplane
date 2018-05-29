package org.freeplane.plugin.configurationservice;

import java.io.File;
import java.util.Map;

import org.freeplane.plugin.script.proxy.Proxy.Controller;
import org.freeplane.plugin.script.proxy.ScriptUtils;

public class ConfigurationSession {

	private Controller c;
	private org.freeplane.plugin.script.proxy.Proxy.Map newHiddenMapFromTemplate;

	public ConfigurationSession() {
		c = ScriptUtils.c();
	}

	void start(String mindmapfile) {
		File templateFile = new File(mindmapfile);
		newHiddenMapFromTemplate = c.newHiddenMapFromTemplate(templateFile);
	}

	public void CreateAttributes(String nodeId, Map<String, Object> attributesMap) {
		newHiddenMapFromTemplate.node(nodeId).setAttributes(attributesMap);
	}

	public void update(String nodeId, String attributeName, int attributeValue) {
		newHiddenMapFromTemplate.node(nodeId).getAttributes().set(attributeName, attributeValue);

	}


}
