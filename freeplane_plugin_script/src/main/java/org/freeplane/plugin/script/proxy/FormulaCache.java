package org.freeplane.plugin.script.proxy;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ExecuteScriptException;

public class FormulaCache implements IExtension{
	private HashMap<String, LinkedHashMap<String, Object>> cache = new HashMap<String, LinkedHashMap<String, Object>>();

	public Object get(NodeModel nodeModel, String text) {
		final LinkedHashMap<String, Object> cacheEntry = cache.get(nodeModel.getID());
		if (cacheEntry == null) return null;
		final Object object = cacheEntry.get(text);
		if(object instanceof ExecuteScriptException){
			throw (ExecuteScriptException)object;
		}
		return object;
	}

	public void put(NodeModel nodeModel, String text, Object value) {
		getOrAdd(nodeModel).put(text, value);
	}

	private LinkedHashMap<String, Object> getOrAdd(NodeModel node) {
		LinkedHashMap<String, Object> cacheEntry = cache.get(node.getID());
		if (cacheEntry == null) {
			cacheEntry = new LinkedHashMap<String, Object>(8);
			cache.put(node.getID(), cacheEntry);
		}
		return cacheEntry;
	}

	public void markAsDirtyIfFormulaNode(NodeModel node) {
		final LinkedHashMap<String, Object> entry = cache.get(node.getID());
		if (entry != null) {
//			System.out.println("clearing cache for " + node);
			entry.clear();
		}
	}
}
