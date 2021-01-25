package org.freeplane.features.link;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.StyleString;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.n3.nanoxml.XMLElement;

public class ConnectorStyleCondition extends ASelectableCondition implements ConnectorChecker{
    static final String FILTER_STYLE = "filter_style";
	static final String NAME = "connector_style_equals";
	final private IStyle value;

	public ConnectorStyleCondition(final IStyle value) {
		this.value = value;
	}

	public boolean checkNode(final NodeModel node) {
	    return NodeConnectorChecker.checkNodeConnectors(node, this);
	}

	public boolean check(final ConnectorModel connector) {
	    return value.equals(connector.getStyle());
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
			return new ConnectorStyleCondition(new StyleString(text));
		}
		final String name = element.getAttribute("LOCALIZED_TEXT", null);
		if (name != null) {
			return new ConnectorStyleCondition(new StyleTranslatedObject(name));
		}
		return null;
	}

    @Override
    protected String createDescription() {
        final String condition = TextUtils.getText(LinkConditionController.CONNECTOR_LABEL);
        final String simpleCondition = TextUtils.getText(FILTER_STYLE);
        return ConditionFactory.createDescription(condition, simpleCondition, value.toString());
    }

	@Override
    protected String getName() {
	    return NAME;
    }
}
