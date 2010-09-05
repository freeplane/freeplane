package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;

public class ConvertibleAttributeValue extends Convertible {
	public ConvertibleAttributeValue(NodeModel nodeModel, String text) {
		super(FormulaUtils.evalAttributeText(nodeModel, text));
		if (getText() == null)
			throw new NullPointerException("ConvertibleAttributeValue's text may not be null");
	}
}
