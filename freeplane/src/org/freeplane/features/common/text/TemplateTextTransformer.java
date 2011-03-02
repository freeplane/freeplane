package org.freeplane.features.common.text;

import org.freeplane.core.util.FreeplaneDate;
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

	public Object transformContent(Object obj, final NodeModel node) {
		if (obj == null)
			return obj;
		final String text = obj.toString();
		if ((obj instanceof String) && (text.length() == 0 || text.charAt(0) == '='))
			return text;
		final String template = textController.getNodeTextTemplate(node);
		final boolean nodeNumbering = textController.getNodeNumbering(node);
		return expandTemplate(obj, node, template, nodeNumbering);
	}

	private String expandTemplate(Object obj, final NodeModel node, final String template, boolean nodeNumbering) {
		final boolean hasTemplate = template != null && template.length() != 0;
		if (!hasTemplate && !nodeNumbering){
			final String originalText = obj.toString();
			return originalText;
		}
		// - if html: strip html header
		// - if number or date format: Convertible.toObject(text)
		// - format/expand
		// - if error: use original text
		// - if nodeNumbering add 
		// - if html: enclose in html tag
		final boolean isHtml = (obj instanceof String) && HtmlUtils.isHtmlNode((String)obj);
		if (isHtml) {
			obj = HtmlUtils.extractRawBody((String)obj);
		}
		if (hasTemplate)
			obj = format(obj, template);
		if (nodeNumbering && !node.isRoot())
			obj = getPathToRoot(node) + " " + obj;
		if (isHtml)
			obj = "<html><head></head><body>" + obj + "</body></html>";
		return obj.toString();
	}

	private String format(final Object obj, final String template) {
		try {
			final PatternFormat format = PatternFormat.guessPatternFormat(template);
			// logging for invalid pattern is done in guessPatternFormat()
			if (format == null)
				return obj.toString();
			final Object toFormat;
			if (format.acceptsDate())
				toFormat = (obj instanceof FreeplaneDate) ? obj : FreeplaneDate.toDate(HtmlUtils.htmlToPlain(obj.toString()));
			else if (format.acceptsNumber())
				toFormat = TextUtils.toNumber(HtmlUtils.htmlToPlain(obj.toString()));
			else
				toFormat = obj.toString();
			return toFormat == null ? obj.toString() : format.format(toFormat);
		}
		catch (Exception e) {
			LogUtils.warn("cannot format " + obj.toString() + " with " + template + ": " + e.getMessage());
			return obj.toString();
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
