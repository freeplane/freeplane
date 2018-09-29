package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class FormulaCache implements IExtension{
	private HashMap<String, LinkedHashMap<String, Object>> cache = new HashMap<String, LinkedHashMap<String, Object>>();
	// don't let caching use too much memory - but currently there are little means to cope with unavailable
	// dependency data. It has to be tested but it should "only" lead to some missing updates.
	private static final boolean ENABLE_CACHING = !Controller.getCurrentController().getResourceController()
	    .getBooleanProperty("formula_disable_caching");

	Object get(NodeModel nodeModel, String text) {
		final LinkedHashMap<String, Object> cacheEntry = cache.get(nodeModel.getID());
		if (cacheEntry == null) return null;
		final Object object = cacheEntry.get(text);
		if(object instanceof ExecuteScriptException){
			throw (ExecuteScriptException)object;
		}
		return object;
	}

	void put(NodeModel nodeModel, String text, Object value) {
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

	void markAsDirtyIfFormulaNode(NodeModel node) {
		final LinkedHashMap<String, Object> entry = cache.get(node.getID());
		if (entry != null) {
//			System.out.println("clearing cache for " + node);
			entry.clear();
		}
	}

	static void manageChange(final ArrayList<NodeModel> dependencies) {
		if (ENABLE_CACHING) {
			for (NodeModel nodeModel : dependencies) {
				getFormulaCache(nodeModel.getMap()).markAsDirtyIfFormulaNode(nodeModel);
			}
		}
	}

	static Object execute(final NodeModel nodeModel, final ScriptContext scriptContext, final String text) {
		final ScriptingPermissions restrictedPermissions = ScriptingPermissions.getFormulaPermissions();
		if (ENABLE_CACHING) {
			final FormulaCache formulaCache = getFormulaCache(nodeModel.getMap());
			Object value = formulaCache.get(nodeModel, text);
			if (value == null) {
				try {
					value = ScriptingEngine.executeScript(nodeModel, text, scriptContext, restrictedPermissions);
					if(value == null)
						throw new ExecuteScriptException("Null pointer returned by formula");
					formulaCache.put(nodeModel, text, value);
				}
				catch (ExecuteScriptException e) {
					formulaCache.put(nodeModel, text, e);
					throw e;
				}
			}
			else {
				scriptContext.accessNode(nodeModel);
			}
			return value;
		}
		else {
			return ScriptingEngine.executeScript(nodeModel, text, scriptContext, restrictedPermissions);
		}
	}

	private static FormulaCache getFormulaCache(MapModel map) {
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
