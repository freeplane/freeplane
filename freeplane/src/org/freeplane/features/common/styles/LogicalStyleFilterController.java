package org.freeplane.features.common.styles;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ConditionFactory;
import org.freeplane.features.common.filter.condition.IElementaryConditionController;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.n3.nanoxml.XMLElement;

public class LogicalStyleFilterController implements IElementaryConditionController {
	static final String FILTER_STYLE = "filter_style";
// // 	private final Controller controller;

	public LogicalStyleFilterController(final Controller controller) {
		super();
//		this.controller = controller;
	}

	public boolean canEditValues(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(LogicalStyleFilterController.FILTER_STYLE);
	}

	public boolean canSelectValues(final Object property, final NamedObject simpleCond) {
		return true;
	}

	public ISelectableCondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                            final Object value, final boolean ignoreCase) {
		return new StyleCondition(value);
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getStyleConditionNames());
	}

	private Object[] getStyleConditionNames() {
		return new NamedObject[] { TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO) };
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_STYLE));
		return list;
	}

	public ComboBoxEditor getValueEditor() {
		return null;
	}

	public ComboBoxModel getValuesForProperty(final Object property) {
		final MapStyleModel mapStyles = MapStyleModel.getExtension(Controller.getCurrentController().getMap());
		final Object[] styles = mapStyles.getStyles().toArray();
		return new DefaultComboBoxModel(styles);
	}

	public boolean isCaseDependent(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public ISelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(StyleCondition.NAME)) {
			return StyleCondition.load(element);
		}
		return null;
	}
}
