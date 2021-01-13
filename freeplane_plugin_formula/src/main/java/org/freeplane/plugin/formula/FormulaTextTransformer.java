package org.freeplane.plugin.formula;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.format.FormattedFormula;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.text.AbstractContentTransformer;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.RichTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.script.FormulaUtils;

class FormulaTextTransformer extends AbstractContentTransformer implements IEditBaseCreator{
	public static final String CONTENT_TYPE_FORMULA = "FormulaContentType";

    FormulaTextTransformer(int priority) {
		super(priority);
	}

    @Override
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
        if (transformedExtension == node.getUserObject() && textController.isTextFormattingDisabled(node))
            return obj;
        final String text = obj.toString();
        if (!FormulaUtils.containsFormula(text)) {
            return obj;
        }
        final String plainText = HtmlUtils.htmlToPlain(text);
        // starting a new ScriptContext in evalIfScript
        final Object result = FormulaUtils.evalIfScript(node, plainText);
        return result;
    }

    @Override
	public boolean isFormula(TextController textController, final Object obj, final NodeModel node,
    		Object transformedExtension) {
    	if (obj instanceof FormattedFormula) {
    		final FormattedFormula formattedFormula = (FormattedFormula) obj;
    		return isFormula(textController, formattedFormula.getObject(), node,transformedExtension);
    	}
    	if (!(obj instanceof String)) {
    		return false;
    	}
    	if (node != null && transformedExtension == node.getUserObject() && textController.isTextFormattingDisabled(node))
    		return false;
    	final String text = obj.toString();
    	if (!FormulaUtils.containsFormula(text)) {
    		return false;
    	}
    	return true;
    }

    @Override
    public EditNodeBase createEditor(final NodeModel node, final EditNodeBase.IEditControl editControl,
            Object content, final boolean editLong) {
        MTextController textController = MTextController.getController();
        String text = textController.getEditedText(node, content, CONTENT_TYPE_FORMULA);
        if(text == null)
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
			textEditor.setBackground(Color.WHITE);
			textEditor.setForeground(Color.BLACK);
			textEditor.setSelectedTextColor(Color.BLUE);
			final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(textEditor);
			scrollPane.setMinimumSize(new Dimension(0, 60));
			final MapExplorerController explorer = Controller.getCurrentModeController().getExtension(MapExplorerController.class);
			final EditNodeDialog editNodeDialog = new FormulaEditor(explorer, node, text, firstKeyEvent, editControl, false, textEditor);
			editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
			textEditor.setContentType("text/groovy");

			final String fontName = ResourceController.getResourceController().getProperty(FormulaEditor.GROOVY_EDITOR_FONT);
			final int fontSize = ResourceController.getResourceController().getIntProperty(FormulaEditor.GROOVY_EDITOR_FONT_SIZE);
			final Font font = UITools.scaleUI(new Font(fontName, Font.PLAIN, fontSize));
			textEditor.setFont(font);

			return editNodeDialog;
		}
		return null;
    }

	@Override
	public boolean markTransformation() {
	    return true;
    }
}
