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
package org.freeplane.features.icon;

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
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class PriorityConditionController implements IElementaryConditionController {
	static final String FILTER_PRIORITY = "filter_priority";
	private static final IconStore STORE = IconStoreFactory.ICON_STORE;

	public PriorityConditionController() {
		super();
	}

	public boolean canEditValues(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(PriorityConditionController.FILTER_PRIORITY);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCondition,
	                                            final Object valueObj, final boolean matchCase,
	                                            final boolean matchApproximately,
                                                final boolean ignoreDiacritics) {
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
		return new DefaultComboBoxModel(new TranslatedObject[] {
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        TranslatedObject.literal(ConditionFactory.FILTER_GT), TranslatedObject.literal(ConditionFactory.FILTER_GE),
		        TranslatedObject.literal(ConditionFactory.FILTER_LE), TranslatedObject.literal(ConditionFactory.FILTER_LT), });
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_PRIORITY));
		return list;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		return null;
	}

	public ComboBoxModel getValuesForProperty(final Object property, TranslatedObject simpleCond) {
		final Object[] items = new Object[10];
		for (int i = 1; i < 10; ++i) {
			items[i - 1] = STORE.getMindIcon("full-" + Integer.toString(i));
		}
		final ComboBoxModel box = new DefaultComboBoxModel(items);
		return box;
	}

	public boolean isCaseDependent(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(PriorityCompareCondition.NAME)) {
			return PriorityCompareCondition.load(element);
		}
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
	    return null;
    }
}
