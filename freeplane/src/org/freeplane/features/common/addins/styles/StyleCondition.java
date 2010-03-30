package org.freeplane.features.common.addins.styles;

import javax.swing.JComponent;

import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ConditionFactory;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class StyleCondition implements ISelectableCondition {
	static final String NAME = "style_equals_condition";
	final private Object value;
	private JComponent renderer;
	private String description;

	public StyleCondition(final Object value) {
		this.value = value;
	}

	public boolean checkNode(final ModeController modeController, final NodeModel node) {
		return LogicalStyleModel.getStyle(node).equals(value);
	}

	public JComponent getListCellRendererComponent() {
		if (renderer == null) {
			renderer = ConditionFactory.createCellRendererComponent(toString());
		}
		return renderer;
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(NAME);
		if (value instanceof String) {
			child.setAttribute("TEXT", (String) value);
		}
		else if (value instanceof NamedObject) {
			child.setAttribute("LOCALIZED_TEXT", ((NamedObject) value).getObject().toString());
		}
		element.addChild(child);
	}

	public static ISelectableCondition load(final XMLElement element) {
		final String text = element.getAttribute("TEXT", null);
		if (text != null) {
			return new StyleCondition(text);
		}
		final String name = element.getAttribute("LOCALIZED_TEXT", null);
		if (name != null) {
			return new StyleCondition(new NamedObject(name));
		}
		return null;
	}

	@Override
	public String toString() {
		if (description == null) {
			description = createDesctiption();
		}
		return description;
	}

	private String createDesctiption() {
		final String filterStyle = TextUtils.getText(LogicalStyleFilterController.FILTER_STYLE);
		return filterStyle + " '" + value.toString() + '\'';
	}
}
