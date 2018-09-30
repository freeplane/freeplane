package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class FormulaCache implements IExtension{
	private HashMap<String, LinkedHashMap<String, CachedResult>> cache = new HashMap<String, LinkedHashMap<String, CachedResult>>();
	// don't let caching use too much memory - but currently there are little means to cope with unavailable
	// dependency data. It has to be tested but it should "only" lead to some missing updates.
	static final boolean ENABLE_CACHING = !Controller.getCurrentController().getResourceController()
	    .getBooleanProperty("formula_disable_caching");

	Object getOrThrowCachedResult(NodeScript nodeScript) {
		final LinkedHashMap<String, CachedResult> cacheEntry = cache.get(nodeScript.node.getID());
		if (cacheEntry == null) return null;
		final CachedResult cachedResult = cacheEntry.get(nodeScript.script);
		return getOrThrowCachedResult(cachedResult.returnedValue);
	}

	private Object getOrThrowCachedResult(Object object) {
		if(object instanceof ExecuteScriptException){
			throw (ExecuteScriptException)object;
		}
		return object;
	}

	void put(NodeScript nodeScript, CachedResult result) {
		getOrAdd(nodeScript.node).put(nodeScript.script, result);
	}

	private LinkedHashMap<String, CachedResult> getOrAdd(NodeModel node) {
		LinkedHashMap<String, CachedResult> cacheEntry = cache.get(node.getID());
		if (cacheEntry == null) {
			cacheEntry = new LinkedHashMap<String, CachedResult>(8);
			cache.put(node.getID(), cacheEntry);
		}
		return cacheEntry;
	}

	void remove(NodeModel node) {
		final LinkedHashMap<String, CachedResult> entry = cache.get(node.getID());
		if (entry != null) {
//			System.out.println("clearing cache for " + node);
			entry.clear();
		}
	}

	static FormulaCache of(MapModel map) {
		FormulaCache formulaCache = map.getExtension(FormulaCache.class);
		if (formulaCache == null) {
			formulaCache = new FormulaCache();
			map.addExtension(formulaCache);
		}
		return formulaCache;
	}
	static void removeFrom(MapModel map) {
		map.removeExtension(FormulaCache.class);
	}


}
