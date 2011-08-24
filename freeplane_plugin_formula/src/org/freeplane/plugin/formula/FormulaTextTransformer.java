package org.freeplane.plugin.formula;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;

import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.AbstractContentTransformer;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.features.text.mindmapmode.KeyEventQueue;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;

class FormulaTextTransformer extends AbstractContentTransformer implements IEditBaseCreator{
	FormulaTextTransformer(int priority) {
		super(priority);
	}

	public Object transformContent(final Object obj, final NodeModel node, Object transformedExtension) {
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
	                                 final EditNodeBase.IEditControl editControl, String text, final boolean editLong) {
		final KeyEvent firstKeyEvent = KeyEventQueue.getInstance().getFirstEvent(); 
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
