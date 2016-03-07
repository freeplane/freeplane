package org.freeplane.features.styles;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class StyleCondition extends ASelectableCondition {
	static final String NAME = "style_equals_condition";
	final private Object value;

	public StyleCondition(final IStyle value) {
		this.value = value;
	}

	public boolean checkNode(final NodeModel node) {
		IStyle firstStyle = LogicalStyleController.getController().getFirstStyle(node);
		return value.equals(firstStyle);
	}

	public void fillXML(final XMLElement child) {
		if (value instanceof StyleString) {
			child.setAttribute("TEXT", value.toString());
		}
		else if (value instanceof StyleTranslatedObject) {
			child.setAttribute("LOCALIZED_TEXT", ((StyleTranslatedObject) value).getObject().toString());
		}
	}

	public static ASelectableCondition load(final XMLElement element) {
		final String text = element.getAttribute("TEXT", null);
		if (text != null) {
			return new StyleCondition(new StyleString(text));
		}
		final String name = element.getAttribute("LOCALIZED_TEXT", null);
		if (name != null) {
			return new StyleCondition(new StyleTranslatedObject(name));
		}
		return null;
	}

	protected String createDescription() {
		final String filterStyle = TextUtils.getText(LogicalStyleFilterController.FILTER_STYLE);
		return filterStyle + " '" + value.toString() + '\'';
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
