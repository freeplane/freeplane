package org.freeplane.features.styles;

import java.util.Collection;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class StyleContainsCondition extends ASelectableCondition {
	static final String NAME = "style_contains_condition";
	final private Object value;

	public StyleContainsCondition(final IStyle value) {
		this.value = value;
	}

	public boolean checkNode(final NodeModel node) {
		final Collection<IStyle> styles = LogicalStyleController.getController().getStyles(node);
		return styles.contains(value);
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
			return new StyleContainsCondition(new StyleString(text));
		}
		final String name = element.getAttribute("LOCALIZED_TEXT", null);
		if (name != null) {
			return new StyleContainsCondition(new StyleTranslatedObject(name));
		}
		return null;
	}

	protected String createDescription() {
		final String style = TextUtils.getText(LogicalStyleFilterController.FILTER_STYLE);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
		return ConditionFactory.createDescription(style, simpleCondition, value.toString());
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
