package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.dependencies.RelatedElements;

public class FormulaCache implements IExtension{
	private final HashMap<String, LinkedHashMap<String, CachedResult>> cache = new HashMap<String, LinkedHashMap<String, CachedResult>>();
	// don't let caching use too much memory - but currently there are little means to cope with unavailable
	// dependency data. It has to be tested but it should "only" lead to some missing updates.
	static final boolean ENABLE_CACHING = !Controller.getCurrentController().getResourceController()
	    .getBooleanProperty("formula_disable_caching");

	static void removeFromCache(final ArrayList<NodeModel> dependencies) {
			if (ENABLE_CACHING) {
				for (final NodeModel nodeModel : dependencies) {
					FormulaCache.of(nodeModel.getMap()).remove(nodeModel);
				}
			}
		}

	Object getOrThrowCachedResult(final NodeScript nodeScript) {
		final LinkedHashMap<String, CachedResult> cacheEntry = cache.get(nodeScript.node.getID());
		if (cacheEntry == null)
			return null;
		final CachedResult cachedResult = cacheEntry.get(nodeScript.script);
		if (cachedResult == null)
			return null;
		return getOrThrowCachedResult(cachedResult.returnedValue);
	}

	private Object getOrThrowCachedResult(final Object object) {
		if(object instanceof ExecuteScriptException){
			throw (ExecuteScriptException)object;
		}
		return object;
	}

	void put(final NodeScript nodeScript, final CachedResult result) {
		getOrAdd(nodeScript.node).put(nodeScript.script, result);
	}

	private LinkedHashMap<String, CachedResult> getOrAdd(final NodeModel node) {
		LinkedHashMap<String, CachedResult> cacheEntry = cache.get(node.getID());
		if (cacheEntry == null) {
			cacheEntry = new LinkedHashMap<String, CachedResult>(8);
			cache.put(node.getID(), cacheEntry);
		}
		return cacheEntry;
	}

	void remove(final NodeModel node) {
		final LinkedHashMap<String, CachedResult> entry = cache.get(node.getID());
		if (entry != null) {
//			System.out.println("clearing cache for " + node);
			entry.clear();
		}
	}

	static FormulaCache of(final MapModel map) {
		FormulaCache formulaCache = map.getExtension(FormulaCache.class);
		if (formulaCache == null) {
			formulaCache = new FormulaCache();
			map.addExtension(formulaCache);
		}
		return formulaCache;
	}
	static void removeFrom(final MapModel map) {
		map.removeExtension(FormulaCache.class);
	}

	RelatedElements getAccessedValues(final NodeModel node, final String script) {
		if(ENABLE_CACHING) {
			final LinkedHashMap<String, CachedResult> cacheEntry = cache.get(node.getID());
			if (cacheEntry == null) return null;
			final CachedResult cachedResult = cacheEntry.get(script);
			if(cachedResult != null)
				return cachedResult.relatedElements;
		}
		return null;
	}


}
