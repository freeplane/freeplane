package org.freeplane.plugin.script.proxy;


import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.common.map.NodeModel;

public class FormulaUtils {
	public static Object evalNodeText(NodeModel nodeModel) {
		// node texts are not-null since this is checked on setText()
		final String text = HtmlUtils.htmlToPlain(nodeModel.getText(), true);
		if (text.length() > 1 && text.charAt(0) == '=')
			return FormulaRegistry.evalNodeTextImpl(text, nodeModel);
		else
			return text;
	}

	public static Object evalAttributeText(final NodeModel nodeModel, final String text) {
		if (text != null && text.length() > 1 && text.charAt(0) == '=')
			// FIXME use evalAttributeTextImpl!
			return FormulaRegistry.evalNodeTextImpl(text, nodeModel);
		else
			return text;
	}

	public static Object evalNoteText(NodeModel nodeModel, String text) {
		if (text != null && text.length() > 1 && text.charAt(0) == '=')
			// FIXME use evalNoteTextImpl!
			return FormulaRegistry.evalNodeTextImpl(text, nodeModel);
		else
			return text;
    }
}
