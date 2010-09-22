package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.freeplane.core.controller.Controller;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.proxy.FormulaCache;

public class FormulaUtils {
	// don't let caching use too much memory - but currently there are little means to cope with unavailable
	// dependency data. It has to be tested but it should "only" lead to some missing updates.
	private static WeakHashMap<MapModel, FormulaCache> formulaCaches = new WeakHashMap<MapModel, FormulaCache>();
	private static WeakHashMap<MapModel, EvaluationDependencies> evaluationDependencies = new WeakHashMap<MapModel, EvaluationDependencies>();
	private static final boolean ENABLE_CACHING = Controller.getCurrentController().getResourceController()
	    .getBooleanProperty("spreadsheet_enable_caching");

	/** evaluate text as a script if it starts with '='.
	 * @return the evaluation result for script and the original text otherwise */
	public static Object evalIfScript(final NodeModel nodeModel, ScriptContext scriptContext, final String text) {
		if (text != null && text.length() > 1 && text.charAt(0) == '=') {
			scriptContext = (scriptContext == null) ? new ScriptContext() : scriptContext;
			return eval(nodeModel, scriptContext, text.substring(1));
		}
		else {
			return text;
		}
	}

	/** evaluate text as a script.
	 * @return the evaluation result. */
	public static Object eval(final NodeModel nodeModel, final ScriptContext scriptContext, final String text) {
		if (ENABLE_CACHING && scriptContext != null) {
			final FormulaCache formulaCache = getFormulaCache(nodeModel.getMap());
			scriptContext.push(nodeModel);
			Object value = formulaCache.get(nodeModel, text);
			if (value == null) {
//				System.out.println("eval(" + text + ")");
				value = ScriptingEngine.executeScript(nodeModel, text, scriptContext);
				formulaCache.put(nodeModel, text, value);
			}
			else {
				scriptContext.accessNode(nodeModel);
			}
			scriptContext.pop();
			return value;
		}
		else {
			return ScriptingEngine.executeScript(nodeModel, text.substring(1), scriptContext);
		}
	}

	public static List<NodeModel> manageChangeAndReturnDependencies(final NodeModel... nodes) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		for (int i = 0; i < nodes.length; i++) {
			final Set<NodeModel> nodeDependencies = getEvaluationDependencies(nodes[i].getMap()).getDependencies(
			    nodes[i]);
			if (nodeDependencies != null)
				dependencies.addAll(nodeDependencies);
		}
		if (ENABLE_CACHING) {
			for (NodeModel nodeModel : dependencies) {
				getFormulaCache(nodeModel.getMap()).markAsDirtyIfFormulaNode(nodeModel);
			}
		}
		return dependencies;
	}

	private static FormulaCache getFormulaCache(MapModel map) {
		FormulaCache formulaCache = formulaCaches.get(map);
		if (formulaCache == null) {
			formulaCache = new FormulaCache();
			formulaCaches.put(map, formulaCache);
		}
		return formulaCache;
	}

	private static EvaluationDependencies getEvaluationDependencies(MapModel map) {
		EvaluationDependencies dependencies = evaluationDependencies.get(map);
		if (dependencies == null) {
			dependencies = new EvaluationDependencies();
			evaluationDependencies.put(map, dependencies);
		}
		return dependencies;
	}

	public static void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
		if (accessingNode != accessedNode)
			getEvaluationDependencies(accessingNode.getMap()).accessNode(accessingNode, accessedNode);
	}

	public static void accessBranch(NodeModel accessingNode, NodeModel accessedNode) {
		getEvaluationDependencies(accessingNode.getMap()).accessBranch(accessingNode, accessingNode);
	}

	public static void accessAll(NodeModel accessingNode) {
		getEvaluationDependencies(accessingNode.getMap()).accessAll(accessingNode);
	}

	public static void clearCache(MapModel map) {
		//		System.out.println("clearing formula cache for " + map.getTitle());
		evaluationDependencies.remove(map);
		formulaCaches.remove(map);
	}
}
