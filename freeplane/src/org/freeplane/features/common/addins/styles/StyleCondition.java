package org.freeplane.features.common.addins.styles;

import javax.swing.JComponent;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.model.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

public class StyleCondition implements ISelectableCondition {
	static final String NAME = "style_equals_condition";
	final private Object value;
	private JComponent renderer;
	private String description;

	public StyleCondition(Object value) {
		this.value = value;
	}

	public boolean checkNode(ModeController modeController, NodeModel node) {
		return LogicalStyleModel.getStyle(node).equals(value);
	}

	public JComponent getListCellRendererComponent() {
		if (renderer == null) {
			renderer = ConditionFactory.createCellRendererComponent(toString());
		}
		return renderer;
	}

	public void toXml(XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(NAME);
		if(value instanceof String){
			child.setAttribute("TEXT", (String) value);
		}
		else if(value instanceof NamedObject){
			child.setAttribute("LOCALIZED_TEXT", ((NamedObject) value).getObject().toString());
		}

		element.addChild(child);
	}

	public static ISelectableCondition load(XMLElement element) {
		String text = element.getAttribute("TEXT", null);
		if(text != null){
			return new StyleCondition(text);
		}
		String name = element.getAttribute("LOCALIZED_TEXT", null);
		if(name != null){
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
		final String filterStyle = ResourceBundles.getText(LogicalStyleFilterController.FILTER_STYLE);
		return filterStyle + " '" + value.toString() + '\'';
	}

}