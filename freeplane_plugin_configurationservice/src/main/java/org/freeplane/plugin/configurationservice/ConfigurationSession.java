package org.freeplane.plugin.configurationservice;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freeplane.plugin.script.proxy.Proxy.Controller;
import org.freeplane.plugin.script.proxy.ScriptUtils;

public class ConfigurationSession {

	private final Controller c;
	private org.freeplane.plugin.script.proxy.Proxy.Map newHiddenMapFromTemplate;

	public ConfigurationSession() {
		c = ScriptUtils.c();
	}

	public void start(String mindmapfile) {
		File templateFile = new File(mindmapfile);
		newHiddenMapFromTemplate = c.newHiddenMapFromTemplate(templateFile);
	}

	public boolean isStarted() {
		return newHiddenMapFromTemplate != null;
	}

	public void CreateAttributes(String nodeId, Map<String, Object> attributesMap) {
		newHiddenMapFromTemplate.node(nodeId).setAttributes(attributesMap);
	}

	public void update(String nodeId, String attributeName, int attributeValue) {
		newHiddenMapFromTemplate.node(nodeId).getAttributes().set(attributeName, attributeValue);
	}

	public Map<String, Object> readValues(String nodeId, List<String> attributesList) {
		Map<String, Object> attributeMap = new HashMap<String, Object>();

		for (String attributeName : attributesList) {
			Object attributeFormula =  newHiddenMapFromTemplate.node(nodeId).getAttributes().getFirst(attributeName);
			Object attributeValue =  newHiddenMapFromTemplate.node(nodeId).getAttributes().getTransformed().getFirst(attributeName);
			attributeMap.put(attributeName, attributeFormula + " => " + attributeValue);
		}
		return attributeMap;
	}

}
