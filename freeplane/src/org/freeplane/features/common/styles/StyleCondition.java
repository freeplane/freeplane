package org.freeplane.features.common.styles;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class StyleCondition extends ASelectableCondition {
	static final String NAME = "style_equals_condition";
	final private Object value;

	public StyleCondition(final Object value) {
		this.value = value;
	}

	public boolean checkNode(final NodeModel node) {
		return LogicalStyleController.getController().getFirstStyle(node).equals(value);
	}

	public void fillXML(final XMLElement child) {
		if (value instanceof String) {
			child.setAttribute("TEXT", (String) value);
		}
		else if (value instanceof StyleNamedObject) {
			child.setAttribute("LOCALIZED_TEXT", ((StyleNamedObject) value).getObject().toString());
		}
	}

	public static ASelectableCondition load(final XMLElement element) {
		final String text = element.getAttribute("TEXT", null);
		if (text != null) {
			return new StyleCondition(text);
		}
		final String name = element.getAttribute("LOCALIZED_TEXT", null);
		if (name != null) {
			return new StyleCondition(new StyleNamedObject(name));
		}
		return null;
	}

	protected String createDesctiption() {
		final String filterStyle = TextUtils.getText(LogicalStyleFilterController.FILTER_STYLE);
		return filterStyle + " '" + value.toString() + '\'';
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
