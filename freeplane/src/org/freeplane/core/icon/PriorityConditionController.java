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
package org.freeplane.core.icon;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class PriorityConditionController implements IElementaryConditionController {
	static final String FILTER_PRIORITY = "filter_priority";
	
	private static final IconStore STORE = IconStoreFactory.create();

	public PriorityConditionController() {
		super();
	}

	public boolean canEditValues(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(PriorityConditionController.FILTER_PRIORITY);
	}

	public boolean canSelectValues(final Object property, final NamedObject simpleCond) {
		return true;
	}

	public ISelectableCondition createCondition(final Object selectedItem, final NamedObject simpleCondition,
	                                            final Object valueObj, final boolean ignoreCase) {
		final String value = ((MindIcon) valueObj).getName().substring(5, 6);
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
			return new PriorityCompareCondition(value, 0, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
			return new PriorityCompareCondition(value, 0, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
			return new PriorityCompareCondition(value, 1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
			return new PriorityCompareCondition(value, -1, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
			return new PriorityCompareCondition(value, -1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
			return new PriorityCompareCondition(value, 1, false);
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new NamedObject[] {
		        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        NamedObject.literal(ConditionFactory.FILTER_GT), NamedObject.literal(ConditionFactory.FILTER_GE),
		        NamedObject.literal(ConditionFactory.FILTER_LE), NamedObject.literal(ConditionFactory.FILTER_LT), });
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(ResourceBundles.createTranslatedString(FILTER_PRIORITY));
		return list;
	}

	public ComboBoxEditor getValueEditor() {
		return null;
	}

	public ComboBoxModel getValuesForProperty(final Object property) {
		final Object[] items = new Object[10];
		for(int i = 1; i < 10; ++i) {
			items[i-1] = STORE.getMindIcon("full-" + Integer.toString(i));
		}
		final ComboBoxModel box = new DefaultComboBoxModel(items);
		return box;
	}

	public boolean isCaseDependent(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public ISelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(PriorityCompareCondition.NAME)) {
			return PriorityCompareCondition.load(element);
		}
		return null;
	}
}
