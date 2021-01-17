/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.attribute;

import java.util.NoSuchElementException;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.ui.components.TypedListCellRenderer;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.DoubleListModel;
import org.freeplane.core.util.collection.ExtendedComboBoxModel;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.TransformationException;
import org.freeplane.features.ui.FrameController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class AttributeConditionController implements IElementaryConditionController {
private static final String ATTRIBUTE_NAME_OR_VALUE = "attribute_name_or_value";
static final TranslatedObject ANY_ATTRIBUTE_NAME_OR_VALUE_OBJECT = new TranslatedObject(ATTRIBUTE_NAME_OR_VALUE);
// // 	final private Controller controller;
	private final ExtendedComboBoxModel values = new ExtendedComboBoxModel();

	public AttributeConditionController() {
		super();
//		this.controller = controller;
	}

	public boolean canEditValues(final Object selectedItem, final TranslatedObject simpleCond) {
		return canSelectValues(selectedItem, simpleCond);
	}

	public boolean canHandle(final Object selectedItem) {
		return ANY_ATTRIBUTE_NAME_OR_VALUE_OBJECT.equals(selectedItem) || selectedItem.getClass().equals(String.class);
	}

	public boolean canSelectValues(final Object selectedItem, final TranslatedObject simpleCond) {
		return !simpleCond.objectEquals(ConditionFactory.FILTER_EXIST)
		        && !simpleCond.objectEquals(ConditionFactory.FILTER_DOES_NOT_EXIST);
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCondition,
	                                            final Object value, final boolean matchCase, final boolean matchApproximately,
                                                final boolean ignoreDiacritics) {
		final Object attribute = selectedItem;
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_EXIST)) {
			return new AttributeExistsCondition(attribute);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_DOES_NOT_EXIST)) {
			return new AttributeNotExistsCondition(attribute);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
		    return new AttributeCompareCondition(attribute, value, matchCase, 0, true, matchApproximately, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
		    return new AttributeCompareCondition(attribute, value, matchCase, 0, false, matchApproximately, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
		    return new AttributeCompareCondition(attribute, value, matchCase, 1, true, false, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
		    return new AttributeCompareCondition(attribute, value, matchCase, -1, false, false, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
		    return new AttributeCompareCondition(attribute, value, matchCase, -1, true, false, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
		    return new AttributeCompareCondition(attribute, value, matchCase, 1, false, false, ignoreDiacritics);
		}
        if (simpleCondition.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
            return new AttributeContainsCondition(attribute, value.toString(), matchCase, matchApproximately, ignoreDiacritics);
        }
        if (simpleCondition.objectEquals(ConditionFactory.FILTER_REGEXP)) {
            return new AttributeMatchesCondition(attribute, value.toString(), matchCase);
        }
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new TranslatedObject[] {
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_CONTAINS),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_REGEXP),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_EXIST),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_DOES_NOT_EXIST),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        TranslatedObject.literal(ConditionFactory.FILTER_GT), TranslatedObject.literal(ConditionFactory.FILTER_GE),
		        TranslatedObject.literal(ConditionFactory.FILTER_LE), TranslatedObject.literal(ConditionFactory.FILTER_LT)
		});
	}

	public ListModel getFilteredProperties() {
		final AttributeRegistry registry = AttributeRegistry.getRegistry(Controller.getCurrentController().getMap());
		final DefaultListModel anyAttributeList = new DefaultListModel();
		anyAttributeList.addElement(ANY_ATTRIBUTE_NAME_OR_VALUE_OBJECT);
		if (registry != null) {
			return new DoubleListModel(anyAttributeList, registry.getListBoxModel());
		}
		return anyAttributeList;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
	    if(selectedCondition.objectEquals(ConditionFactory.FILTER_CONTAINS) 
                || selectedCondition.objectEquals(ConditionFactory.FILTER_REGEXP) )
            return new FixedBasicComboBoxEditor();
	    return FrameController.getTextDateTimeEditor();
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem, TranslatedObject simpleCond) {
		final MapModel map = Controller.getCurrentController().getMap();
		final AttributeRegistry registry = AttributeRegistry.getRegistry(map);
		try {
            final AttributeRegistryElement element = registry.getElement(selectedItem.toString());
            final SortedComboBoxModel list = element.getValues();
            SortedComboBoxModel linkedList = new SortedComboBoxModel();
            for(int i = 0; i < list.getSize();i++){
            	final Object value = list.getElementAt(i);
            	final TextController textController = TextController.getController();
            	if (! textController.isFormula(value)) {
					try {
						final Object transformedValue = textController.getTransformedObject(map.getRootNode(), element, value);
						linkedList.add(transformedValue);
					} catch (TransformationException e) {
						LogUtils.severe(e);
					}
            	}
            }
            values.setExtensionList(linkedList);
        }
        catch (NoSuchElementException e) {
            values.setExtensionList(null);
        }
		return values;
	}

	public boolean isCaseDependent(final Object selectedItem, final TranslatedObject simpleCond) {
		return true;
	}
	
	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(AttributeCompareCondition.NAME)) {
			return AttributeCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(AttributeExistsCondition.NAME)) {
			return AttributeExistsCondition.load(element);
		}
        if (element.getName().equalsIgnoreCase(AttributeNotExistsCondition.NAME)) {
            return AttributeNotExistsCondition.load(element);
        }
        if (element.getName().equalsIgnoreCase(AttributeContainsCondition.NAME)) {
            return AttributeContainsCondition.load(element);
        }
        if (element.getName().equalsIgnoreCase(AttributeMatchesCondition.NAME)) {
            return AttributeMatchesCondition.load(element);
        }
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
        if(selectedCondition.objectEquals(ConditionFactory.FILTER_CONTAINS) 
                || selectedCondition.objectEquals(ConditionFactory.FILTER_REGEXP) )
            return null;
	    return new TypedListCellRenderer();
    }

	static Object toAttributeObject(final String attribute) {
	    final Object xmlElementAttribute = attribute != null ? attribute : ANY_ATTRIBUTE_NAME_OR_VALUE_OBJECT;
	    return xmlElementAttribute;
	}

}
