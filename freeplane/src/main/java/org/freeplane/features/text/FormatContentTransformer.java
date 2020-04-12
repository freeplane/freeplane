package org.freeplane.features.text;

import java.util.List;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;

class FormatContentTransformer extends AbstractContentTransformer {
	final private TextController textController;

	public FormatContentTransformer(final TextController textController, final int priority) {
		super(priority);
		this.textController = textController;
	}

	public Object transformContent(TextController textController, Object obj, final NodeModel node, Object transformedExtension) {
		if (obj == null || node == null || node.getUserObject() != transformedExtension)
			return obj;
		final String format = textController.getNodeFormat(node);
		final boolean nodeNumbering = textController.getNodeNumbering(node);
		return expandFormat(obj, node, format, nodeNumbering);
	}

	private Object expandFormat(Object obj, final NodeModel node, final String format, boolean nodeNumbering) {
		final boolean hasFormat = format != null && format.length() != 0 && 
				!PatternFormat.IDENTITY_PATTERN.equals(format) && !PatternFormat.STANDARD_FORMAT_PATTERN.equals(format);
		if (!hasFormat && !nodeNumbering){
			return obj;
		}
		// - if html: strip html header
		// - if number or date format: Scanner.scan
		// - format/expand
		// - if error: use original text
		// - if nodeNumbering add numbering
		// - if html: enclose in html tag
		final boolean isHtml = (obj instanceof String) && HtmlUtils.isHtml((String)obj);
		if (isHtml) {
			obj = HtmlUtils.extractRawBody((String)obj);
		}
		if (hasFormat)
			obj = FormatController.format(obj, format);
		if (nodeNumbering && !node.isRoot()){
			StringBuilder builder = new StringBuilder(node.getNodeLevel() * 2);
			addNumbers(builder, node);
			builder.append(' ');
			if (isHtml) {
				obj = insertPrefix(obj.toString(), builder.toString());
			}
			else{
				obj = builder.toString() + obj;
			}
		}
		if (isHtml)
			obj = "<html><head></head><body>" + obj + "</body></html>";
		return obj.toString();
	}

	private String insertPrefix(String html, String prefix) {
		StringBuilder sb = new StringBuilder(html.length() + prefix.length() + 1);
		int i = 0;
		int level = 0;
		WHILE: while(i < html.length()){
			final char c = html.charAt(i);
			switch(c){
				case '<': level++; break;
				case '>': level--; break;
				default:
					if(level == 0 && ! Character.isWhitespace(c))
						break  WHILE;
			}
			i++;
		}
		sb.append(html.subSequence(0, i));
		sb.append(prefix);
		sb.append(html.subSequence(i, html.length()));
		return sb.toString();
    }

	private void addNumbers(StringBuilder builder, NodeModel node) {
		final NodeModel parentNode = node.getParentNode();
		if(parentNode == null)
			return;
		addMajorNumbers(parentNode, builder);
		final List<NodeModel> children = parentNode.getChildren();
		int counter = 1;
		for (NodeModel child : children) {
			if(child.createID().equals(node.createID()))
				break;
			if(textController.getNodeNumbering(child))
				counter++;
		}
		builder.append(counter);
	}

	private void addMajorNumbers(final NodeModel node, StringBuilder builder) {
		if(SummaryNode.isSummaryNode(node)) {
			final NodeModel summaryParentNode = node.getParentNode();
			if(summaryParentNode == null)
				return;
			addMajorNumbers(summaryParentNode, builder);
		} 
		else if( textController.getNodeNumbering(node)){
			addNumbers(builder, node);
			if (builder.length() > 0)
				builder.append('.');
		}
	}
}
