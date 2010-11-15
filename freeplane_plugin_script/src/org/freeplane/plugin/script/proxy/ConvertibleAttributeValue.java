package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.ScriptContext;

public class ConvertibleAttributeValue extends Convertible {
	public ConvertibleAttributeValue(NodeModel nodeModel, ScriptContext scriptContext, String text) throws ExecuteScriptException {
		super(FormulaUtils.evalIfScript(nodeModel, scriptContext, text));
// this seems to be annoying since for loops over node lists with not all having a certain attribute its easier
// to allow such convertibles
//		if (getText() == null)
//			throw new NullPointerException(TextUtils.format("formula.error.attributeValueIsNull", (text == null ? "null"
//			        : text)));
	}
}
