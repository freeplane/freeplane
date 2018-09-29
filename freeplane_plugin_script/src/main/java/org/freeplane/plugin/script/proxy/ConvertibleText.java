package org.freeplane.plugin.script.proxy;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.ScriptContext;

public class ConvertibleText extends Convertible {
	public ConvertibleText(NodeModel nodeModel, ScriptContext scriptContext, String text) {
		super(FormulaUtils.evalIfScript(nodeModel, text));
// this seems to be annoying since for loops over node lists with not all having a certain attribute its easier
// to allow such convertibles
//		if (getText() == null)
//			throw new NullPointerException(TextUtils.format("formula.error.attributeValueIsNull", (text == null ? "null"
//			        : text)));
	}
}
