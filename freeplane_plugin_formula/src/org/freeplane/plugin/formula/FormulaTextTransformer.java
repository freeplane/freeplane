package org.freeplane.plugin.formula;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;

import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.AbstractTextTransformer;
import org.freeplane.features.mindmapmode.text.EditNodeBase;
import org.freeplane.features.mindmapmode.text.EditNodeDialog;
import org.freeplane.features.mindmapmode.text.IEditBaseCreator;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;

class FormulaTextTransformer extends AbstractTextTransformer implements IEditBaseCreator{
	FormulaTextTransformer(int priority) {
		super(priority);
	}

	public Object transformContent(final Object obj, final NodeModel node) {
		if (! (obj instanceof String)) {
			return obj;
		}
		final String text = obj.toString();
		final String plainText = HtmlUtils.htmlToPlain(text);
		if (!FormulaUtils.containsFormula(plainText)) {
			return text;
		}
		// starting a new ScriptContext in evalIfScript
		final Object result = FormulaUtils.evalIfScript(node, null, plainText);
		if (result == null) {
			throw new ExecuteScriptException("got null result from evaluating " + node.getID() + ", text='"
			        + plainText.substring(1) + "'");
		}
		return result.toString();
	}

	public EditNodeBase createEditor(final NodeModel node, final EditedComponent editedComponent,
	                                 final EditNodeBase.IEditControl editControl, String text, final InputEvent firstEvent,
	                                 final boolean editLong) {
		final KeyEvent firstKeyEvent; 
		if(firstEvent instanceof KeyEvent)
			firstKeyEvent = (KeyEvent) firstEvent;
		else
			firstKeyEvent = null;
		if(firstKeyEvent != null){
			if (firstKeyEvent.getKeyChar() == '='){
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
			final EditNodeDialog editNodeDialog = new FormulaEditor(node, text, firstKeyEvent, editControl, false, textEditor);
			editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
			textEditor.setContentType("text/groovy");
			return editNodeDialog;
		}
		return null;
    }
}
