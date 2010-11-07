package org.freeplane.plugin.formula;

import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.ITextTransformer;
import org.freeplane.features.mindmapmode.text.EditNodeBase;
import org.freeplane.features.mindmapmode.text.EditNodeDialog;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.JSyntaxPaneProxy;

class FormulaTextTransformer implements ITextTransformer {
	FormulaTextTransformer() {
	}

	public String transformText(final String text, final NodeModel nodeModel) {
		if (text == null) {
			return text;
		}
		final String plainText = HtmlUtils.htmlToPlain(text);
		if(! FormulaUtils.containsFormula(plainText)){
			return text;
		}
		// starting a new ScriptContext in evalIfScript
		final Object result = FormulaUtils.evalIfScript(nodeModel, null, plainText);
		if (result == null) {
			throw new ExecuteScriptException("got null result from evaluating " + nodeModel.getID() + ", text='"
			        + plainText.substring(1) + "'");
		}
		return result.toString();
	}

	public EditNodeBase createEditNodeBase(NodeModel nodeModel, String text, IEditControl editControl, KeyEvent firstEvent,
	                                       boolean isNewNode, boolean editLong) {
		if(firstEvent != null){
			if (firstEvent.getKeyChar() == '='){
				text = "=";
			}
			else{
				return null;
			}
		}
		if(text.startsWith("=")){
			JSyntaxPaneProxy.init();
			JEditorPane textEditor = new JEditorPane();
			final EditNodeDialog editNodeDialog = new EditNodeDialog(nodeModel, text, firstEvent, editControl, false, textEditor);
			editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
			textEditor.setContentType("text/groovy");
			return editNodeDialog;
		}
		return null;
    }
}
