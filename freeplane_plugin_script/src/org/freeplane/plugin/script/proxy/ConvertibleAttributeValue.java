package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.ScriptContext;

public class ConvertibleAttributeValue extends Convertible {
	public ConvertibleAttributeValue(NodeModel nodeModel, ScriptContext scriptContext, String text) {
		super(FormulaUtils.evalIfScript(nodeModel, scriptContext, text));
		if (getText() == null)
			throw new NullPointerException("ConvertibleAttributeValue's text may not be null");
	}
}
