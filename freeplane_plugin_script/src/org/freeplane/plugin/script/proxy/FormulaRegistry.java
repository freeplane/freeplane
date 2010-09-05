package org.freeplane.plugin.script.proxy;

import java.util.HashMap;

import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.ScriptingEngine;

public class FormulaRegistry {
	public enum Access {
		NODE, BRANCH, ALL
    }

	// FIXME: avoid statics by attaching it to the Map
	private static HashMap<MapModel, FormulaCache> nodeTextFormulaCaches = new HashMap<MapModel, FormulaCache>();
	private static HashMap<MapModel, NodeDependencies> nodeDependencies = new HashMap<MapModel, NodeDependencies>();
	

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
	
	public void accessNode(NodeProxy node) {
		final NodeModel nodeModel = node.getDelegate();
		getDependencies(nodeModel.getMap()).addDependency(nodeModel, Access.NODE);
	}

	public void accessBranch(NodeProxy node) {
		final NodeModel nodeModel = node.getDelegate();
		getDependencies(nodeModel.getMap()).addDependency(nodeModel, Access.BRANCH);
	}
	
	public void accessAll(NodeProxy node) {
		final NodeModel nodeModel = node.getDelegate();
		getDependencies(nodeModel.getMap()).addDependency(nodeModel, Access.ALL);
	}

	private NodeDependencies getDependencies(MapModel map) {
		NodeDependencies dependencies = nodeDependencies.get(map);
		if (dependencies == null) {
			dependencies = new NodeDependencies();
			nodeDependencies.put(map, dependencies);
		}
	    return dependencies;
    }
}
