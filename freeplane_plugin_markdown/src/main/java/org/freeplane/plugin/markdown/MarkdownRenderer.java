package org.freeplane.plugin.markdown;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.AbstractContentTransformer;
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
		super(20);
	}

	@Override
	public Object transformContent(TextController textController,
			Object content, NodeModel node, Object transformedExtension)
			throws TransformationException {
        if (!(content instanceof String)) {
            return content;
        }
        if (transformedExtension == node.getUserObject()) {
            if (textController.isTextFormattingDisabled(node))
                return content;
            String nodeFormat = textController.getNodeFormat(node);
            if(! MarkdownFormat.MARKDOWN_FORMAT.equals(nodeFormat))
                return content;
        }
        if(transformedExtension == node.getUserObject()){
            String markdown = (String) content;
            String html = "<html>" + Marked.marked(markdown);
            return html;
          }
        return content;
	}

	@Override
	public EditNodeBase createEditor(NodeModel node,
			IEditControl editControl, String text, boolean editLong) {
		MTextController textController = MTextController.getController();
		if (textController.isTextFormattingDisabled(node)) // Format=Text!
			return null;
		final KeyEvent firstKeyEvent = textController.getEventQueue().getFirstEvent();
		String nodeFormat = textController.getNodeFormat(node);

		// this option has been added to work around bugs in JSyntaxPane with Chinese characters
		if (ResourceController.getResourceController().getBooleanProperty(MARKDOWN_EDITOR_DISABLE))
			return null;
        if(MarkdownFormat.MARKDOWN_FORMAT.equals(nodeFormat)){
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
		return null;
	}
}
