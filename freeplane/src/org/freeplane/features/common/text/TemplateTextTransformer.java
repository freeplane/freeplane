package org.freeplane.features.common.text;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.format.PatternFormat;
import org.freeplane.features.common.map.NodeModel;

class TemplateTextTransformer extends AbstractTextTransformer {
	final private TextController textController;

	public TemplateTextTransformer(final TextController textController, final int priority) {
		super(priority);
		this.textController = textController;
	}

	public String transformText(String text, final NodeModel node) {
		if (text == null || text.length() == 0 || text.charAt(0) == '=')
			return text;
		final String template = textController.getNodeTextTemplate(node);
		final boolean nodeNumbering = textController.getNodeNumbering(node);
		return expandTemplate(text, node, template, nodeNumbering);
	}

	private String expandTemplate(String text, final NodeModel node, final String template, boolean nodeNumbering) {
		final String originalText = text;
		final boolean hasTemplate = template != null && template.length() != 0;
		if (!hasTemplate && !nodeNumbering)
			return originalText;
		// - if html: strip html header
		// - if number or date format: Convertible.toObject(text)
		// - format/expand
		// - if error: use original text
		// - if nodeNumbering add 
		// - if html: enclose in html tag
		final boolean isHtml = HtmlUtils.isHtmlNode(text);
		if (isHtml) {
			text = HtmlUtils.extractRawBody(text);
		}
		if (hasTemplate)
			text = format(text, template);
		if (nodeNumbering)
			text = getPathToRoot(node) + " " + text;
		if (isHtml)
			text = "<html><head></head><body>" + text + "</body></html>";
		return text;
	}

	private String format(final String text, final String template) {
		try {
			final PatternFormat format = PatternFormat.guessPatternFormat(template);
			// logging for invalid pattern is done in guessPatternFormat()
			if (format == null)
				return text;
			final Object toFormat;
			if (format.acceptsDate())
				toFormat = TextUtils.toDate(HtmlUtils.htmlToPlain(text));
			else if (format.acceptsNumber())
				toFormat = TextUtils.toNumber(HtmlUtils.htmlToPlain(text));
			else
				toFormat = text;
			return toFormat == null ? text : format.format(toFormat);
		}
		catch (Exception e) {
			LogUtils.warn("cannot format " + text + " with " + template + ": " + e.getMessage());
			return text;
		}
	}

	private String getPathToRoot(NodeModel node) {
		final NodeModel[] pathToRoot = node.getPathToRoot();
		if (pathToRoot.length < 2)
			return "";
		StringBuilder builder = new StringBuilder(pathToRoot.length * 2);
		for (int i = 1; i < pathToRoot.length; i++) {
			if (builder.length() > 0)
				builder.append('.');
			builder.append(pathToRoot[i].getParentNode().getIndex(pathToRoot[i]) + 1);
		}
		if (pathToRoot.length == 1)
			builder.append('.');
		return builder.toString();
	}
}
