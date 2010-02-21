package org.freeplane.features.common.addins.styles;

import java.util.Collection;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

public class LogicalStyleFilterController implements
		IElementaryConditionController {

	static final String FILTER_STYLE = "filter_style";

	private final Controller controller;
	public LogicalStyleFilterController(Controller controller) {
		super();
		this.controller = controller;
	}

	public boolean canEditValues(Object property, NamedObject simpleCond) {
		return false;
	}

	public boolean canHandle(Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(LogicalStyleFilterController.FILTER_STYLE);
	}

	public boolean canSelectValues(Object property, NamedObject simpleCond) {
		return true;
	}

	public ISelectableCondition createCondition(Object selectedItem,
			NamedObject simpleCond, Object value, boolean ignoreCase) {
		return new StyleCondition(value);
	}

	public ComboBoxModel getConditionsForProperty(Object property) {
		return new DefaultComboBoxModel(getStyleConditionNames());
	}

	private Object[] getStyleConditionNames() {
		return new NamedObject[] { ResourceBundles.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO)};
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(ResourceBundles.createTranslatedString(FILTER_STYLE));
		return list;
	}

	public ComboBoxEditor getValueEditor() {
		return null;
	}

	public ComboBoxModel getValuesForProperty(Object property) {
		MapStyleModel mapStyles = MapStyleModel.getExtension(controller.getMap());
		Object[]  styles= mapStyles.getStyles().toArray();
		return new DefaultComboBoxModel(styles);
	}

	public boolean isCaseDependent(Object property, NamedObject simpleCond) {
		return false;
	}

	public ISelectableCondition loadCondition(XMLElement element) {
		if (element.getName().equalsIgnoreCase(StyleCondition.NAME)) {
			return StyleCondition.load(element);
		}
		return null;
	}

}
