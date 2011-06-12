package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.proxy.FormulaCache;

public class FormulaUtils {
	// don't let caching use too much memory - but currently there are little means to cope with unavailable
	// dependency data. It has to be tested but it should "only" lead to some missing updates.
	private static final boolean ENABLE_CACHING = !Controller.getCurrentController().getResourceController()
	    .getBooleanProperty("formula_disable_caching");

	/** evaluate text as a script if it starts with '='.
	 * @return the evaluation result for script and the original text otherwise 
	 * @throws ExecuteScriptException */
	public static Object evalIfScript(final NodeModel nodeModel, ScriptContext scriptContext, final String text){
		if (containsFormula(text)) {
			scriptContext = (scriptContext == null) ? new ScriptContext() : scriptContext;
			return eval(nodeModel, scriptContext, text.substring(1));
		}
		else {
			return text;
		}
	}

	public static boolean containsFormula(final String text) {
	    return text != null && text.length() > 1 && text.charAt(0) == '=';
    }

	/** evaluate text as a script.
	 * @return the evaluation result. 
	 * @throws ExecuteScriptException */
	public static Object eval(final NodeModel nodeModel, final ScriptContext scriptContext, final String text) {
		//		System.err.println(nodeModel.getID() + ": " + text);
		if (!scriptContext.push(nodeModel, text)) {
			throw new StackOverflowError(TextUtils.format("formula.error.circularReference",
			    HtmlUtils.htmlToPlain(scriptContext.getStackFront().getText())));
		}
		final boolean restrictedPermissions = true;
		try {
			if (ENABLE_CACHING) {
				final FormulaCache formulaCache = getFormulaCache(nodeModel.getMap());
				Object value = formulaCache.get(nodeModel, text);
				if (value == null) {
					//				System.out.println("eval(" + text + ")");
					try {
						value = ScriptingEngine.executeScript(nodeModel, text, scriptContext, restrictedPermissions);
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
		finally {
			scriptContext.pop();
		}
	}

	public static List<NodeModel> manageChangeAndReturnDependencies(boolean includeChanged, final NodeModel... nodes) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		for (int i = 0; i < nodes.length; i++) {
			final LinkedHashSet<NodeModel> nodeDependencies = new LinkedHashSet<NodeModel>(0);
			getEvaluationDependencies(nodes[i].getMap()).getDependencies(nodeDependencies, nodes[i]);
			if (nodeDependencies != null)
				dependencies.addAll(nodeDependencies);
			if (includeChanged)
				dependencies.add(nodes[i]);
		}
		if (ENABLE_CACHING) {
			for (NodeModel nodeModel : dependencies) {
				getFormulaCache(nodeModel.getMap()).markAsDirtyIfFormulaNode(nodeModel);
			}
		}
		return dependencies;
	}

	private static FormulaCache getFormulaCache(MapModel map) {
		FormulaCache formulaCache = (FormulaCache) map.getExtension(FormulaCache.class);
		if (formulaCache == null) {
			formulaCache = new FormulaCache();
			map.addExtension(formulaCache);
		}
		return formulaCache;
	}

	private static EvaluationDependencies getEvaluationDependencies(MapModel map) {
		EvaluationDependencies dependencies = (EvaluationDependencies) map.getExtension(EvaluationDependencies.class);
		if (dependencies == null) {
			dependencies = new EvaluationDependencies();
			map.addExtension(dependencies);
		}
		return dependencies;
	}

	public static void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
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
		map.removeExtension(FormulaCache.class);
		map.removeExtension(EvaluationDependencies.class);
	}
}
