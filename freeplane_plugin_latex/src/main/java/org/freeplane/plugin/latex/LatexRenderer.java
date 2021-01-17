package org.freeplane.plugin.latex;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JEditorPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
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
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXIcon;

public class LatexRenderer extends AbstractContentTransformer implements IEditBaseCreator {

	private static final String LATEX_EDITOR_FONT_SIZE = "latex_editor_font_size";
	private static final String LATEX_EDITOR_FONT = "latex_editor_font";
	private static final String LATEX_EDITOR_DISABLE = "latex_disable_editor";
	private static final String LATEX = "\\latex";
	private static final String UNPARSED_LATEX = "\\unparsedlatex";
	static final String LATEX_FORMAT = "latexPatternFormat";
	static final String UNPARSED_LATEX_FORMAT = "unparsedLatexPatternFormat";


	public LatexRenderer() {
		super(20);
	}

	@Override
	public Object transformContent(NodeModel node,
			Object nodeProperty, Object content, TextController textController)
			throws TransformationException {
		final String latext = getText(node, nodeProperty, content, TargetMode.FOR_ICON, textController);
		if (latext == null)
			return content;
		final NodeStyleController ncs = NodeStyleController.getController(textController.getModeController());
		final int maxWidth = ncs.getMaxWidth(node).toBaseUnitsRounded();
		TeXText teXt = new TeXText(latext);
		int fontSize = Math.round(ncs.getFontSize(node) * UITools.FONT_SCALE_FACTOR);
		TeXIcon icon = teXt.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize, TeXConstants.ALIGN_LEFT, maxWidth);
		return icon;
	}

	private static boolean checkForLatexPrefix(final String nodeText, final String prefix)
	{
		int startLength = prefix.length() + 1;
		return nodeText.length() > startLength && nodeText.startsWith(prefix) &&
			 Character.isWhitespace(nodeText.charAt(startLength - 1));
	}

	private static enum TargetMode { FOR_ICON, FOR_EDITOR };

	@Override
	public EditNodeBase createEditor(NodeModel node,
			Object nodeProperty, Object content, IEditControl editControl, boolean editLong) {
		// this option has been added to work around bugs in JSyntaxPane with Chinese characters
		if (ResourceController.getResourceController().getBooleanProperty(LATEX_EDITOR_DISABLE))
			return null;
        MTextController textController = MTextController.getController();
        String latexText = getText(node, nodeProperty, content, TargetMode.FOR_EDITOR, textController);
        if(latexText == null)
            return null;

		final KeyEvent firstKeyEvent = textController.getEventQueue().getFirstEvent();


		JEditorPane textEditor = new JEditorPane();
		textEditor.setBackground(Color.WHITE);
		textEditor.setForeground(Color.BLACK);
		textEditor.setSelectedTextColor(Color.BLUE);
		textEditor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(textEditor);
		scrollPane.setMinimumSize(new Dimension(0, 60));
		final EditNodeDialog editNodeDialog = new LatexEditor(node, latexText, firstKeyEvent, editControl, false, textEditor);
		editNodeDialog.setTitle(TextUtils.getText("latex_editor"));
		textEditor.setContentType("text/latex");

		final String fontName = ResourceController.getResourceController().getProperty(LATEX_EDITOR_FONT);
		final int fontSize = ResourceController.getResourceController().getIntProperty(LATEX_EDITOR_FONT_SIZE);
		final Font font = UITools.scaleUI(new Font(fontName, Font.PLAIN, fontSize));
		textEditor.setFont(font);

		return editNodeDialog;
	}

	private String getText(NodeModel node, Object nodeProperty, Object content, TargetMode targetMode, TextController textController) {
		if(! (content instanceof String))
			return null;
		MNoteController noteController = MNoteController.getController();
		String nodeFormat;
		if (nodeProperty instanceof NodeModel) {
		    if (! textController.isTextFormattingDisabled(node)) {
		        nodeFormat = textController.getNodeFormat(node);
		    } else
		        return  null;
		} else if (nodeProperty instanceof DetailModel && LatexRenderer.LATEX_FORMAT.equals(textController.getDetailsContentType(node))
		        || nodeProperty instanceof NoteModel && LatexRenderer.LATEX_FORMAT.equals(noteController.getNoteContentType(node))) {
		    nodeFormat = LatexRenderer.LATEX_FORMAT;
		}
		else
		    return  null;
		String plainOrHtmlText = (String) content;
		String text = HtmlUtils.htmlToPlain(plainOrHtmlText);
		String latexText = getLatexText(text, nodeFormat, targetMode);
		return latexText;
	}
	private String getLatexText(final String nodeText, final String nodeFormat, final TargetMode mode)
	{
		boolean includePrefix = mode == TargetMode.FOR_EDITOR;

		if(checkForLatexPrefix(nodeText, LATEX)){
			return includePrefix ? nodeText : nodeText.substring(LATEX.length() + 1);
		}
		else if(LatexRenderer.LATEX_FORMAT.equals(nodeFormat)){
			return nodeText;
		} else if(checkForLatexPrefix(nodeText, UNPARSED_LATEX) && mode == TargetMode.FOR_EDITOR) {
			return nodeText;
		} else if(LatexRenderer.UNPARSED_LATEX_FORMAT.equals(nodeFormat) && mode == TargetMode.FOR_EDITOR) {
			return nodeText;
		} else {
			return null;
		}
	}


}
