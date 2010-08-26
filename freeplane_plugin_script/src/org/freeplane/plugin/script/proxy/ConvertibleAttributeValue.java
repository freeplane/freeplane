package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;

public class ConvertibleAttributeValue extends Convertible {
	private final NodeModel nodeModel;

	public ConvertibleAttributeValue(NodeModel nodeModel, String text) {
		super(text);
		if (text == null)
			throw new NullPointerException("ConvertibleAttributeValue's text may not be null");
		this.nodeModel = nodeModel;
	}
	
	public Convertible getValue() {
		return new Convertible(FormulaUtils.evalAttributeText(nodeModel, getText()));
	}
}
