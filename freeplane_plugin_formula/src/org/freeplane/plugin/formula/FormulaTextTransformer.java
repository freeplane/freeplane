package org.freeplane.plugin.formula;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;

import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.format.FormattedFormula;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.AbstractContentTransformer;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;

class FormulaTextTransformer extends AbstractContentTransformer implements IEditBaseCreator{
	FormulaTextTransformer(int priority) {
		super(priority);
	}

    public Object transformContent(TextController textController, final Object obj, final NodeModel node,
                                   Object transformedExtension) {
        if (obj instanceof FormattedFormula) {
            final FormattedFormula formattedFormula = (FormattedFormula) obj;
            final Object evaluationResult = transformContent(textController, formattedFormula.getObject(), node,
                transformedExtension);
            return new FormattedObject(evaluationResult, formattedFormula.getPattern());
        }
        if (!(obj instanceof String)) {
            return obj;
        }
        if (textController.isTextFormattingDisabled(node))
            return obj;
        final String text = obj.toString();
        if (!FormulaUtils.containsFormulaCheckHTML(text)) {
            return obj;
        }
        final String plainText = HtmlUtils.htmlToPlain(text);
        // starting a new ScriptContext in evalIfScript
        final Object result = FormulaUtils.evalIfScript(node, null, plainText);
        if (result == null) {
            throw new ExecuteScriptException("got null result from evaluating " + node.getID() + ", text='"
                    + plainText.substring(1) + "'");
        }
        return result;
    }

	public EditNodeBase createEditor(final NodeModel node, final EditNodeBase.IEditControl editControl,
	                                 String text, final boolean editLong) {
		MTextController textController = MTextController.getController();
		if (textController.isTextFormattingDisabled(node))
			return null;
		final KeyEvent firstKeyEvent = textController.getEventQueue().getFirstEvent(); 
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
			textEditor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(textEditor);
			scrollPane.setMinimumSize(new Dimension(0, 60));
			final EditNodeDialog editNodeDialog = new FormulaEditor(node, text, firstKeyEvent, editControl, false, textEditor);
			editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
			textEditor.setContentType("text/groovy");
			return editNodeDialog;
		}
		return null;
    }
	
	public boolean markTransformation() {
	    return true;
    }
}
