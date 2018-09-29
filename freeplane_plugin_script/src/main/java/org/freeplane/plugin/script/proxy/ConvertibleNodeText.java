package org.freeplane.plugin.script.proxy;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.ScriptContext;

/**
 * A {@link Convertible} subclass as return type of {@link Node#getTo()}. Note that the nodeModel is only
 * accessible for formula evaluation; Node.text is only read on construction, therefore text and nodeModel
 * are not synchronized.
 */
public class ConvertibleNodeText extends Convertible {
	/**
	 * Uses the text or, in case of HTML nodes, plain text of the node as a basis.
	 * 
	 * Note that nodeModel.getText() will never return null since nodeModel.setText(null)
	 * will result in a NullPointerException. So there's nothing to check here.
	 * @throws ExecuteScriptException 
	 */
	public ConvertibleNodeText(NodeModel nodeModel, ScriptContext scriptContext) {
		super(FormulaUtils.evalIfScript(nodeModel, HtmlUtils.htmlToPlain(nodeModel.getText(), true)));
	}
}
