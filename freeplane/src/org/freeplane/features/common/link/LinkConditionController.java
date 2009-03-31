/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.common.link;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.util.IListModel;
import org.freeplane.core.filter.util.SortedMapListModel;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public class LinkConditionController implements IElementaryConditionController {
	static final String FILTER_LINK = "filter_link";
	private final ComboBoxEditor editor = new BasicComboBoxEditor();
	private final ComboBoxModel values = new DefaultComboBoxModel();

	public boolean canEditValues(final Object property, final NamedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(FILTER_LINK);
	}

	public boolean canSelectValues(final Object property, final NamedObject simpleCond) {
		return true;
	}

	public ICondition createCondition(final Object selectedItem, final NamedObject simpleCond, final Object value,
	                                  final boolean ignoreCase) {
		if (simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
			return new HyperLinkEqualsCondition((String) value);
		}
		if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
			return new HyperLinkContainsCondition((String) value);
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getLinkConditionNames());
	}

	public IListModel getFilteredProperties() {
		final SortedMapListModel list = new SortedMapListModel();
		list.add(FreeplaneResourceBundle.createTranslatedString(FILTER_LINK));
		return list;
	}

	private Object[] getLinkConditionNames() {
		return new NamedObject[] { FreeplaneResourceBundle.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        FreeplaneResourceBundle.createTranslatedString(ConditionFactory.FILTER_CONTAINS) };
	}

	public ComboBoxEditor getValueEditor() {
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object property) {
		return values;
	}

	public boolean isCaseDependent(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public ICondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(HyperLinkEqualsCondition.NAME)) {
			final String target = element.getAttribute("text", null);
			return new HyperLinkEqualsCondition(target);
		}
		if (element.getName().equalsIgnoreCase(HyperLinkContainsCondition.NAME)) {
			final String target = element.getAttribute("text", null);
			return new HyperLinkContainsCondition(target);
		}
		return null;
	}
}
