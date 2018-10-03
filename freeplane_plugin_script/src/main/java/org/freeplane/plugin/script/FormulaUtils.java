package org.freeplane.plugin.script;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class FormulaUtils {

	/** evaluate text as a script if it starts with '='.
	 * @return the evaluation result for script and the original text otherwise
	 * @throws ExecuteScriptException */
	public static Object evalIfScript(final NodeModel nodeModel, final String text){
		if (textContainsFormula(text)) {
			final String script = text.substring(1);
			return eval(nodeModel, script);
		}
		else {
			return text;
		}
	}

    public static Object safeEvalIfScript(final NodeModel nodeModel, String text) {
        try {
            return evalIfScript(nodeModel, text);
        }
        catch (Exception e) {
            LogUtils.info("could not interpret as a formula (ignored): " + text + " due to " + e.getMessage());
            return text;
        }
    }

	public static boolean textContainsFormula(final String text) {
		// ignore == and => since these are often used in text
		return startsWithEqualSign(text) && secondCharIsntSpecial(text.charAt(1));
	}

	private static boolean startsWithEqualSign(final String text) {
		return text != null && text.length() > 2 && text.charAt(0) == '=';
	}

	private static boolean secondCharIsntSpecial(char secondChar) {
		return secondChar != '=' && secondChar != '>';
	}

	public static boolean containsFormula(Object object) {
		return (object instanceof String) && containsFormula((String)object);
	}

	public static boolean containsFormula(String text) {
	    if(HtmlUtils.isHtmlNode(text))
	    	return htmlContainsFormula(text);
	    else
	    	return textContainsFormula(text);
    }

	private static Pattern FIRST_CHARACTER_IN_HTML = Pattern.compile("(?m)>\\s*[^<\\s]");
	private static boolean htmlContainsFormula(String text) {
	    final Matcher matcher = FIRST_CHARACTER_IN_HTML.matcher(text);
		return matcher.find() && text.charAt(matcher.end()-1) == '=';
    }

	/** evaluate text as a script.
	 * @return the evaluation result.
	 * @throws ExecuteScriptException */
	private static Object eval(final NodeModel nodeModel, final String script) {
		NodeScript nodeScript = new NodeScript(nodeModel, script);
		ScriptContext scriptContext = new ScriptContext(nodeScript);
		if (!FormulaThreadLocalStack.INSTANCE.push(nodeScript)) {
			throw new StackOverflowError(TextUtils.format("formula.error.circularReference",
			    HtmlUtils.htmlToPlain(script)));
		}
		try {
			return eval(scriptContext, nodeScript);
		}
		finally {
			FormulaThreadLocalStack.INSTANCE.pop();
		}
	}

	private static Object eval(ScriptContext scriptContext, NodeScript nodeScript) {
			final ScriptingPermissions restrictedPermissions = ScriptingPermissions.getFormulaPermissions();
			NodeModel node = nodeScript.node;
			String script = nodeScript.script;
			if (FormulaCache.ENABLE_CACHING) {
				final FormulaCache formulaCache = FormulaCache.of(nodeScript.getMap());
				Object value = formulaCache.getOrThrowCachedResult(nodeScript);
				if (value == null) {
					try {
						value = ScriptingEngine.executeScript(node, script, scriptContext, restrictedPermissions);
						if(value == null)
							throw new ExecuteScriptException("Null pointer returned by formula");
						formulaCache.put(nodeScript, new CachedResult(value, scriptContext.getAccessedValues()));
					}
					catch (ExecuteScriptException e) {
						formulaCache.put(nodeScript, new CachedResult(e, scriptContext.getAccessedValues()));
						throw e;
					}
				}
				return value;
			}
			else {
				return ScriptingEngine.executeScript(node, script, scriptContext, restrictedPermissions);
			}

	}

	public static AccessedValues getAccessedValues(NodeModel node, String script) {
		return FormulaCache.of(node.getMap()).getAccessedValues(node, script);
	}

	public static void clearCache(MapModel map) {
		FormulaCache.removeFrom(map);
		EvaluationDependencies.removeFrom(map);
	}
}
