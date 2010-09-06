package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;

public class ConvertibleNodeText extends Convertible {
	/**
	 * Uses the text or, in case of HTML nodes, plain text of the node as a basis.
	 * 
	 * Note that nodeModel.getText() will never return null since nodeModel.setText(null)
	 * will result in a NullPointerException. So there's nothing to check here.
	 */
	public ConvertibleNodeText(NodeModel nodeModel) {
		super(FormulaUtils.evalNodeText(nodeModel));
	}
}
