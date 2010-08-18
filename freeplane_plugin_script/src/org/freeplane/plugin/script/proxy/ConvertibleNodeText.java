package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;

public class ConvertibleNodeText extends Convertible {
	private final NodeModel nodeModel;

	public ConvertibleNodeText(NodeModel nodeModel) {
		super(nodeModel.getText());
		this.nodeModel = nodeModel;
	}
	
	/** without a calculation rule or NodeModel - there's nothing to evaluate. */
	public Object getValue() {
		return FormulaUtils.evalNodeText(nodeModel);
	}
}
