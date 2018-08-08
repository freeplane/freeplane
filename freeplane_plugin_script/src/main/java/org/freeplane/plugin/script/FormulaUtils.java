package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.proxy.FormulaCache;

public class FormulaUtils {
	// don't let caching use too much memory - but currently there are little means to cope with unavailable
	// dependency data. It has to be tested but it should "only" lead to some missing updates.
	private static final boolean ENABLE_CACHING = !Controller.getCurrentController().getResourceController()
	    .getBooleanProperty("formula_disable_caching");
    static final boolean DEBUG_FORMULA_EVALUATION = false;

	/** evaluate text as a script if it starts with '='.
	 * @return the evaluation result for script and the original text otherwise
	 * @throws ExecuteScriptException */
	public static Object evalIfScript(final NodeModel nodeModel, ScriptContext scriptContext, final String text){
		if (containsFormula(text)) {
			scriptContext = (scriptContext == null) ? new ScriptContext(nodeModel.getMap().getURL()) : scriptContext;
			return eval(nodeModel, scriptContext, text.substring(1));
		}
		else {
			return text;
		}
	}

    public static Object safeEvalIfScript(final NodeModel nodeModel, ScriptContext scriptContext, String text) {
        try {
            return evalIfScript(nodeModel, scriptContext, text);
        }
        catch (Exception e) {
            LogUtils.info("could not interpret as a formula (ignored): " + text + " due to " + e.getMessage());
            return text;
        }
    }

	public static boolean containsFormula(final String text) {
		// ignore == and => since these are often used in text
		return startsWithEqualSign(text) && secondCharIsntSpecial(text.charAt(1));
	}

	private static boolean startsWithEqualSign(final String text) {
		return text != null && text.length() > 2 && text.charAt(0) == '=';
	}

	private static boolean secondCharIsntSpecial(char secondChar) {
		return secondChar != '=' && secondChar != '>';
	}

	public static boolean containsFormulaCheckHTML(String text) {
	    if(HtmlUtils.isHtmlNode(text))
	    	return htmlContainsFormula(text);
	    else
	    	return containsFormula(text);
    }

	private static Pattern FIRST_CHARACTER_IN_HTML = Pattern.compile("(?m)>\\s*[^<\\s]");
	private static boolean htmlContainsFormula(String text) {
	    final Matcher matcher = FIRST_CHARACTER_IN_HTML.matcher(text);
		return matcher.find() && text.charAt(matcher.end()-1) == '=';
    }

	/** evaluate text as a script.
	 * @return the evaluation result.
	 * @throws ExecuteScriptException */
	public static Object eval(final NodeModel nodeModel, final ScriptContext scriptContext, final String text) {
	    if (DEBUG_FORMULA_EVALUATION)
	        System.err.println("eval " + nodeModel.getID() + ": " + text);
		if (!scriptContext.push(nodeModel, text)) {
			throw new StackOverflowError(TextUtils.format("formula.error.circularReference",
			    HtmlUtils.htmlToPlain(scriptContext.getStackFront().getText())));
		}
		final ScriptingPermissions restrictedPermissions = ScriptingPermissions.getFormulaPermissions();
		try {
			if (ENABLE_CACHING) {
				final FormulaCache formulaCache = getFormulaCache(nodeModel.getMap());
				Object value = formulaCache.get(nodeModel, text);
				if (value == null) {
					try {
						value = ScriptingEngine.executeScript(nodeModel, text, scriptContext, restrictedPermissions);
						if(value == null)
							throw new ExecuteScriptException("Null pointer returned by formula");
						formulaCache.put(nodeModel, text, value);
						if (DEBUG_FORMULA_EVALUATION)
						    System.err.println("eval: cache miss: recalculated: " + text);
					}
					catch (ExecuteScriptException e) {
						formulaCache.put(nodeModel, text, e);
				        if (DEBUG_FORMULA_EVALUATION)
				            System.err.println("eval: cache miss: exception for: " + text);
						throw e;
					}
				}
				else {
			        if (DEBUG_FORMULA_EVALUATION)
			            System.err.println("eval: cache hit for: " + text);
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
		final ArrayList<NodeModel> dependencies = getAllDependencies(includeChanged, nodes);
		manageChange(dependencies);
		return dependencies;
	}

	public static List<NodeModel> manageChangeAndReturnGlobalDependencies(MapModel map) {
		final ArrayList<NodeModel> dependencies = getGlobalDependencies(map);
		manageChange(dependencies);
		return dependencies;
	}

	private static void manageChange(final ArrayList<NodeModel> dependencies) {
		if (ENABLE_CACHING) {
			for (NodeModel nodeModel : dependencies) {
				getFormulaCache(nodeModel.getMap()).markAsDirtyIfFormulaNode(nodeModel);
			}
		}
	}

	private static ArrayList<NodeModel> getAllDependencies(boolean includeChanged, final NodeModel... nodes) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		for (int i = 0; i < nodes.length; i++) {
			final LinkedHashSet<NodeModel> nodeDependencies = new LinkedHashSet<NodeModel>(0);
			EvaluationDependencies.of(nodes[i].getMap()).getDependencies(nodeDependencies, nodes[i]);
			if (nodeDependencies != null)
				dependencies.addAll(nodeDependencies);
			if (includeChanged)
				dependencies.add(nodes[i]);
		}
		return dependencies;
	}


	private static ArrayList<NodeModel> getGlobalDependencies(MapModel map) {
		final ArrayList<NodeModel> dependencies = new ArrayList<NodeModel>();
		final LinkedHashSet<NodeModel> nodeDependencies = new LinkedHashSet<NodeModel>(0);
		EvaluationDependencies.of(map).getGlobalDependencies(nodeDependencies);
		if (nodeDependencies != null)
			dependencies.addAll(nodeDependencies);
		return dependencies;
	}

	private static FormulaCache getFormulaCache(MapModel map) {
		FormulaCache formulaCache = map.getExtension(FormulaCache.class);
		if (formulaCache == null) {
			formulaCache = new FormulaCache();
			map.addExtension(formulaCache);
		}
		return formulaCache;
	}

	public static void accessNode(NodeModel accessingNode, NodeModel accessedNode) {
		EvaluationDependencies.of(accessedNode.getMap()).accessNode(accessingNode, accessedNode);
	}

	public static void accessBranch(NodeModel accessingNode, NodeModel accessedNode) {
		EvaluationDependencies.of(accessedNode.getMap()).accessBranch(accessingNode, accessedNode);
	}

	public static void accessAll(NodeModel accessingNode) {
		EvaluationDependencies.of(accessingNode.getMap()).accessAll(accessingNode);
	}

	public static void accessGlobalNode(NodeModel accessingNode) {
		EvaluationDependencies.of(accessingNode.getMap()).accessGlobalNode(accessingNode);
	}


	public static void clearCache(MapModel map) {
        if (DEBUG_FORMULA_EVALUATION)
            System.out.println("clearing formula cache for " + map.getTitle());
		map.removeExtension(FormulaCache.class);
		map.removeExtension(EvaluationDependencies.class);
	}
}
