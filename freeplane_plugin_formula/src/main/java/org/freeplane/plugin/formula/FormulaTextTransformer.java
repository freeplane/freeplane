package org.freeplane.plugin.formula;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

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
        JEditorPane textEditor = createTextEditorPane(this::createScrollPane, node, nodeProperty, content);
        return textEditor == null ? null :createEditor(node, editControl, textEditor);
    }

    private JRestrictedSizeScrollPane createScrollPane() {
        final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane();
        scrollPane.setMinimumSize(new Dimension(0, 60));
        return scrollPane;
    }

    @Override
    public JEditorPane createTextEditorPane(Supplier<JScrollPane> scrollPaneSupplier, final NodeModel node, Object nodeProperty,
            Object content) {
        String text = getEditedText(node, nodeProperty, content, MTextController.getController());
        if(text == null)
            return null;
        JEditorPane textEditor = new JEditorPane();
        scrollPaneSupplier.get().setViewportView(textEditor);
        textEditor.setContentType("text/groovy");
        textEditor.setText(text);
        textEditor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        textEditor.setBackground(Color.WHITE);
        textEditor.setForeground(Color.BLACK);
        textEditor.setSelectedTextColor(Color.BLUE);
        final String fontName = ResourceController.getResourceController().getProperty(FormulaEditor.GROOVY_EDITOR_FONT);
        final int fontSize = ResourceController.getResourceController().getIntProperty(FormulaEditor.GROOVY_EDITOR_FONT_SIZE);
        final Font font = UITools.scaleUI(new Font(fontName, Font.PLAIN, fontSize));
        textEditor.setFont(font);
        return textEditor;
    }

    private EditNodeBase createEditor(final NodeModel node,
            final EditNodeBase.IEditControl editControl, JEditorPane textEditor) {
        final MapExplorerController explorer = Controller.getCurrentModeController().getExtension(MapExplorerController.class);
        final KeyEvent firstKeyEvent = MTextController.getController().getEventQueue().getFirstEvent();
        final EditNodeDialog editNodeDialog = new FormulaEditor(explorer, node, firstKeyEvent, editControl, false, textEditor);
        editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
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
		if (nodeProperty instanceof NodeModel && textController.isTextFormattingDisabled(node)
			|| nodeProperty instanceof DetailModel && TextController.CONTENT_TYPE_HTML.equals(textController.getDetailsContentType(node))
		    || nodeProperty instanceof NoteModel && TextController.CONTENT_TYPE_HTML.equals(noteController.getNoteContentType(node)))
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
