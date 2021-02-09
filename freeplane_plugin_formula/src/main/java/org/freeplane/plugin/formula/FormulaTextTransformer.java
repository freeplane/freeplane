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
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.format.FormattedFormula;
import org.freeplane.features.format.FormattedObject;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.text.AbstractContentTransformer;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.script.FormulaUtils;

class FormulaTextTransformer extends AbstractContentTransformer implements IEditBaseCreator{
	private static final String CONTENT_TYPE_FORMULA = TextController.CONTENT_TYPE_AUTO;

    FormulaTextTransformer(int priority) {
		super(priority);
	}

    @Override
	public Object transformContent(final NodeModel node, Object nodeProperty, final Object obj,
                                   TextController textController, Mode mode) {
        if (obj instanceof FormattedFormula) {
            final FormattedFormula formattedFormula = (FormattedFormula) obj;
            final Object evaluationResult = transformContent(node, nodeProperty, formattedFormula.getObject(),
                textController, mode);
            return new FormattedObject(evaluationResult, formattedFormula.getPattern());
        }
        final String text = getViewedText(node, nodeProperty, obj, textController);
        if (text == null || !FormulaUtils.containsFormula(text)) {
            return obj;
        }
        final String plainText = HtmlUtils.htmlToPlain(text);
        // starting a new ScriptContext in evalIfScript
        final Object result = FormulaUtils.evalIfScript(node, plainText);
        return result;
    }

    @Override
	public boolean isFormula(final Object obj) {
    	if (obj instanceof FormattedFormula) {
    		final FormattedFormula formattedFormula = (FormattedFormula) obj;
    		return isFormula(formattedFormula.getObject());
    	}
    	if (!(obj instanceof String)) {
    		return false;
    	}
    	final String text = obj.toString();
    	if (!FormulaUtils.containsFormula(text)) {
    		return false;
    	}
    	return true;
    }

    @Override
    public EditNodeBase createEditor(final NodeModel node, Object nodeProperty,
            Object content, final EditNodeBase.IEditControl editControl, final boolean editLong) {
        MTextController textController = MTextController.getController();
        String text = getEditedText(node, nodeProperty, content, textController);
        if(text == null)
            return null;
        JEditorPane textEditor = new JEditorPane();
        textEditor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        textEditor.setBackground(Color.WHITE);
        textEditor.setForeground(Color.BLACK);
        textEditor.setSelectedTextColor(Color.BLUE);
        final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(textEditor);
        scrollPane.setMinimumSize(new Dimension(0, 60));
        final MapExplorerController explorer = Controller.getCurrentModeController().getExtension(MapExplorerController.class);
        final KeyEvent firstKeyEvent = textController.getEventQueue().getFirstEvent();
        final EditNodeDialog editNodeDialog = new FormulaEditor(explorer, node, text, firstKeyEvent, editControl, false, textEditor);
        editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
        textEditor.setContentType("text/groovy");

        final String fontName = ResourceController.getResourceController().getProperty(FormulaEditor.GROOVY_EDITOR_FONT);
        final int fontSize = ResourceController.getResourceController().getIntProperty(FormulaEditor.GROOVY_EDITOR_FONT_SIZE);
        final Font font = UITools.scaleUI(new Font(fontName, Font.PLAIN, fontSize));
        textEditor.setFont(font);

        return editNodeDialog;
    }

	private String getEditedText(final NodeModel node, Object nodeProperty, Object content, MTextController textController) {
		if (nodeProperty instanceof NodeModel || nodeProperty instanceof NodeAttributeTableModel) {
		    if (! textController.isTextFormattingDisabled(node)) {
		        final KeyEvent firstKeyEvent = textController.getEventQueue().getFirstEvent();
	            if (firstKeyEvent != null && firstKeyEvent.getKeyChar() == '='){
	            	return "=";
	            }
		    }
		}
		return getViewedText(node, nodeProperty, content, textController);

	}
		private String getViewedText(final NodeModel node, Object nodeProperty, Object content, TextController textController) {
		if(! (content instanceof String))
			return null;
		MNoteController noteController = MNoteController.getController();
		if (! ((nodeProperty instanceof NodeModel || nodeProperty instanceof NodeAttributeTableModel) && ! textController.isTextFormattingDisabled(node)
				|| nodeProperty instanceof DetailModel && CONTENT_TYPE_FORMULA.equals(textController.getDetailsContentType(node))
		        || nodeProperty instanceof NoteModel && CONTENT_TYPE_FORMULA.equals(noteController.getNoteContentType(node))))
			return null;
		String plainOrHtmlText = (String)content;
		String text = HtmlUtils.htmlToPlain(plainOrHtmlText);
        if(! FormulaUtils.containsFormula(text))
        	return null;
		return text;
	}

	@Override
	public boolean markTransformation() {
	    return true;
    }
}
