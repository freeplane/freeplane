package org.freeplane.features.common.text;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleModel;

public class TemplateTextTransformer extends AbstractTextTransformer {
	public TemplateTextTransformer(final int priority) {
		super(priority);
	}

	public String transformText(String text, final NodeModel node) {
		if (text == null || text.length() == 0 || text.charAt(0) == '=')
			return text;
		final String template = NodeStyleModel.getNodeTextTemplate(node);
		final Boolean nodeNumbering = NodeStyleModel.getNodeNumbering(node);
		return expandTemplate(text, node, template, (nodeNumbering != null && nodeNumbering));
	}

	private String expandTemplate(String text, NodeModel node, String template, boolean nodeNumbering) {
		System.err.println("FIXME: implement transformTextImpl(" + text + ", " + template + ")");
		// TODO:
		// - if html: strip html header
		// - if number or date format: Convertible.toObject(text)
		// - format/expand
		// - if error: use original text
		// - if nodeNumbering add 
		// - if html: enclose in html tag
		if (template != null)
			text = template.replace("%s", text);
		if (nodeNumbering)
			text = getPathToRoot(node) + " " + text;
		return text;
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
