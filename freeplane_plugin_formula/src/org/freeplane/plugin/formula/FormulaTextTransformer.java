package org.freeplane.plugin.formula;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.ITextTransformer;
import org.freeplane.features.mindmapmode.text.EditNodeBase;
import org.freeplane.features.mindmapmode.text.EditNodeDialog;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;

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
		try {
	        final Object result = FormulaUtils.evalIfScript(nodeModel, null, plainText);
	        if (result == null) {
	        	throw new ExecuteScriptException("got null result from evaluating " + nodeModel.getID() + ", text='"
	        	        + plainText.substring(1) + "'");
	        }
	        return result.toString();
        }
        catch (ExecuteScriptException e) {
	        throw new RuntimeException(e);
        }
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
			JEditorPane textEditor = new JEditorPane();
			final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(textEditor);
			scrollPane.setMinimumSize(new Dimension(0, 60));
			final EditNodeDialog editNodeDialog = new FormulaEditor(nodeModel, text, firstEvent, editControl, false, textEditor);
			editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
			textEditor.setContentType("text/groovy");
			return editNodeDialog;
		}
		return null;
    }
}
