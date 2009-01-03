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
package org.freeplane.map.attribute;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.util.ExtendedComboBoxModel;
import org.freeplane.core.filter.util.IListModel;
import org.freeplane.core.io.XMLElement;
import org.freeplane.core.resources.NamedObject;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class AttributeConditionController implements IElementaryConditionController {
	private final ExtendedComboBoxModel values = new ExtendedComboBoxModel();

	public boolean canEditValues(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		return selectedItem.getClass().equals(String.class);
	}

	public boolean canSelectValues(final Object selectedItem, final NamedObject simpleCond) {
		return !simpleCond.objectEquals(ConditionFactory.FILTER_EXIST)
		        && !simpleCond.objectEquals(ConditionFactory.FILTER_DOES_NOT_EXIST);
	}

	public ICondition createCondition(final Object selectedItem, final NamedObject simpleCondition,
	                                  final Object v, final boolean ignoreCase) {
		final String attribute = (String) selectedItem;
		final String value = (String) v;
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_EXIST)) {
			return new AttributeExistsCondition(attribute);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_DOES_NOT_EXIST)) {
			return new AttributeNotExistsCondition(attribute);
		}
		if (ignoreCase) {
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new AttributeCompareCondition(attribute, value, true, 0, true);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
				return new AttributeCompareCondition(attribute, value, true, 0, false);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
				return new AttributeCompareCondition(attribute, value, true, 1, true);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
				return new AttributeCompareCondition(attribute, value, true, -1, false);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
				return new AttributeCompareCondition(attribute, value, true, -1, true);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
				return new AttributeCompareCondition(attribute, value, true, 1, false);
			}
		}
		else {
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new AttributeCompareCondition(attribute, value, false, 0, true);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
				return new AttributeCompareCondition(attribute, value, false, 0, false);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
				return new AttributeCompareCondition(attribute, value, false, 1, true);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
				return new AttributeCompareCondition(attribute, value, false, -1, false);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
				return new AttributeCompareCondition(attribute, value, false, -1, true);
			}
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
				return new AttributeCompareCondition(attribute, value, false, 1, false);
			}
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new NamedObject[] {
		        Controller.getResourceController().createTranslatedString(
		            ConditionFactory.FILTER_EXIST),
		        Controller.getResourceController().createTranslatedString(
		            ConditionFactory.FILTER_DOES_NOT_EXIST),
		        Controller.getResourceController().createTranslatedString(
		            ConditionFactory.FILTER_IS_EQUAL_TO),
		        Controller.getResourceController().createTranslatedString(
		            ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        NamedObject.literal(ConditionFactory.FILTER_GT),
		        NamedObject.literal(ConditionFactory.FILTER_GE),
		        NamedObject.literal(ConditionFactory.FILTER_LE),
		        NamedObject.literal(ConditionFactory.FILTER_LT) });
	}

	public IListModel getFilteredProperties() {
		return AttributeRegistry.getRegistry(Controller.getController().getMap())
		    .getListBoxModel();
	}

	public ComboBoxEditor getValueEditor() {
		return new BasicComboBoxEditor();
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem) {
		values.setExtensionList(AttributeRegistry.getRegistry(Controller.getController().getMap())
		    .getElement(selectedItem.toString()).getValues());
		return values;
	}

	public boolean isCaseDependent(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public ICondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(AttributeCompareCondition.NAME)) {
			return AttributeCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(AttributeExistsCondition.NAME)) {
			return AttributeExistsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(AttributeNotExistsCondition.NAME)) {
			return AttributeNotExistsCondition.load(element);
		}
		return null;
	}
}
