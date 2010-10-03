package org.freeplane.plugin.script.proxy;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.freeplane.features.common.map.NodeModel;

public class FormulaCache {
	private HashMap<NodeModel, LinkedHashMap<String, Object>> cache = new HashMap<NodeModel, LinkedHashMap<String, Object>>();

	public Object get(NodeModel nodeModel, String text) {
		final LinkedHashMap<String, Object> cacheEntry = cache.get(nodeModel);
		return cacheEntry == null ? null : cacheEntry.get(text);
	}

	public void put(NodeModel nodeModel, String text, Object value) {
		getOrAdd(nodeModel).put(text, value);
	}

	private LinkedHashMap<String, Object> getOrAdd(NodeModel node) {
		LinkedHashMap<String, Object> cacheEntry = cache.get(node);
		if (cacheEntry == null) {
			cacheEntry = new LinkedHashMap<String, Object>(8);
			cache.put(node, cacheEntry);
		}
		return cacheEntry;
	}

	public void markAsDirtyIfFormulaNode(NodeModel node) {
		final LinkedHashMap<String, Object> entry = cache.get(node);
		if (entry != null) {
			System.out.println("clearing cache for " + node);
			entry.clear();
		}
	}
}
