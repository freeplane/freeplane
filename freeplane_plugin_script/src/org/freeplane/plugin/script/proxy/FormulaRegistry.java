package org.freeplane.plugin.script.proxy;

import java.util.HashMap;

import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.ScriptingEngine;

public class FormulaRegistry {
	private static HashMap<MapModel, FormulaCache> nodeTextFormulaCaches = new HashMap<MapModel, FormulaCache>();

	static Object evalNodeTextImpl(String text, NodeModel nodeModel) {
    	final FormulaCache formulaCache = FormulaRegistry.getFormulaCache(nodeModel.getMap());
    	Object value = formulaCache.get(nodeModel.getID());
    	if (value == null) {
    		value = ScriptingEngine.executeScript(nodeModel, text.substring(1));
    		formulaCache.put(nodeModel.getID(), value);
    	}
    	return value;
    }

	private static FormulaCache getFormulaCache(MapModel map) {
    	FormulaCache formulaCache = nodeTextFormulaCaches.get(map);
    	if (formulaCache == null) {
    		formulaCache = new FormulaCache();
    		nodeTextFormulaCaches.put(map, formulaCache);
    	}
    	return formulaCache;
    }
}
