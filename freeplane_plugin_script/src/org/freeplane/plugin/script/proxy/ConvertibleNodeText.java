package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;

public class ConvertibleNodeText extends Convertible {
	private final NodeModel nodeModel;

	/** note that nodeModel.getText() will never return null since nodeModel.setText(null)
	 * will result in a NullPointerException. So there's nothing to check here. */
	public ConvertibleNodeText(NodeModel nodeModel) {
		super(nodeModel.getText());
		this.nodeModel = nodeModel;
	}
	
	public Convertible getValue() {
		return new Convertible(FormulaUtils.evalNodeText(nodeModel));
	}
}
