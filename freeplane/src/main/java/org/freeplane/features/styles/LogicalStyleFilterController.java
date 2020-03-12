package org.freeplane.features.styles;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;

public class LogicalStyleFilterController implements IElementaryConditionController {
	static final String FILTER_STYLE = "filter_style";
// // 	private final Controller controller;

	public LogicalStyleFilterController() {
		super();
//		this.controller = controller;
	}

	public boolean canEditValues(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(LogicalStyleFilterController.FILTER_STYLE);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
	                                            final Object value, final boolean matchCase, final boolean matchApproximately,
                                                final boolean ignoreDiacritics) {
		if(simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO))
			return new StyleCondition((IStyle) value);
		if(simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS))
			return new StyleContainsCondition((IStyle) value);
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getStyleConditionNames());
	}

	private Object[] getStyleConditionNames() {
		return new TranslatedObject[] { TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO) , 
				TextUtils.createTranslatedString(ConditionFactory.FILTER_CONTAINS) };
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_STYLE));
		return list;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		return null;
	}

	public ComboBoxModel getValuesForProperty(final Object property, TranslatedObject simpleCond) {
		final MapStyleModel mapStyles = MapStyleModel.getExtension(Controller.getCurrentController().getMap());
		return mapStyles.getStylesAsComboBoxModel();
	}

	public boolean isCaseDependent(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(StyleCondition.NAME)) {
			return StyleCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(StyleContainsCondition.NAME)) {
			return StyleContainsCondition.load(element);
		}
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
	    return null;
    }
}
