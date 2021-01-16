package org.freeplane.plugin.markdown;

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
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.AbstractContentTransformer;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.RichTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.TransformationException;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.features.text.mindmapmode.MTextController;

import io.github.gitbucket.markedj.Marked;

public class MarkdownRenderer extends AbstractContentTransformer implements IEditBaseCreator {

	private static final String MARKDOWN_EDITOR_FONT_SIZE = "markdown_editor_font_size";
	private static final String MARKDOWN_EDITOR_FONT = "markdown_editor_font";
	private static final String MARKDOWN_EDITOR_DISABLE = "markdown_disable_editor";


	public MarkdownRenderer() {
		super(30);
	}

	@Override
	public Object transformContent(NodeModel node,
			Object nodeProperty, Object content, TextController textController)
			throws TransformationException {
        String text = getText(node, nodeProperty, content, textController);
		if(text == null)
        	return content;
        
        String markdown = (String) text;
        String html = "<html>" + Marked.marked(markdown);
        return html;
	}

	@Override
	public EditNodeBase createEditor(NodeModel node,
			Object nodeProperty, Object content, IEditControl editControl, boolean editLong) {
		MTextController textController = MTextController.getController();
        String text = getText(node, nodeProperty, content, textController);
        if(text == null)
            return null;
        if (ResourceController.getResourceController().getBooleanProperty(MARKDOWN_EDITOR_DISABLE))
        	return null;

		final KeyEvent firstKeyEvent = textController.getEventQueue().getFirstEvent();

		// this option has been added to work around bugs in JSyntaxPane with Chinese characters
		JEditorPane textEditor = new JEditorPane();
		textEditor.setBackground(Color.WHITE);
		textEditor.setForeground(Color.BLACK);
		textEditor.setSelectedTextColor(Color.BLUE);
		textEditor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(textEditor);
		scrollPane.setMinimumSize(new Dimension(0, 60));
		final EditNodeDialog editNodeDialog = new MarkdownEditor(node, text, firstKeyEvent, editControl, false, textEditor);
		editNodeDialog.setTitle(TextUtils.getText("markdown_editor"));
		textEditor.setContentType("text/markdown");

		final String fontName = ResourceController.getResourceController().getProperty(MARKDOWN_EDITOR_FONT);
		final int fontSize = ResourceController.getResourceController().getIntProperty(MARKDOWN_EDITOR_FONT_SIZE);
		final Font font = UITools.scaleUI(new Font(fontName, Font.PLAIN, fontSize));
		textEditor.setFont(font);

		return editNodeDialog;
	}

	private String getText(NodeModel node, Object nodeProperty, Object content, TextController textController) {
		if(! (content instanceof String))
			return null;
		NoteController noteController = NoteController.getController();
		if (nodeProperty instanceof NodeModel) {
		    if (textController.isTextFormattingDisabled(node))
				return null;
		} else if (!(nodeProperty instanceof DetailModel && MarkdownFormat.MARKDOWN_FORMAT.equals(textController.getDetailsContentType(node))
		        || nodeProperty instanceof NoteModel && MarkdownFormat.MARKDOWN_FORMAT.equals(noteController.getNoteContentType(node))))
		    return  null;
		String plainOrHtmlText = (String) content;
		String text = HtmlUtils.htmlToPlain(plainOrHtmlText);
		return text;
	}
}
