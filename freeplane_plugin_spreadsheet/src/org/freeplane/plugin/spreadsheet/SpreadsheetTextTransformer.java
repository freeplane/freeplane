package org.freeplane.plugin.spreadsheet;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.ITextTransformer;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;

class SpreadsheetTextTransformer implements ITextTransformer {
	SpreadsheetTextTransformer() {
	}

	// FIXME: do we actually need a null check here? - wouldn't a NPE be fine?
	public String transform(String text, NodeModel nodeModel) {
		final String plainText = HtmlUtils.htmlToPlain(nodeModel.getText());
		// starting a new ScriptContext in evalIfScript
		final Object result = FormulaUtils.evalIfScript(nodeModel, null, plainText);
		if (result == null) {
			throw new ExecuteScriptException("got null result from evaluating " + nodeModel.getID() + ", text='"
			        + plainText.substring(1) + "'");
		}
		return result.toString();
	}
}
