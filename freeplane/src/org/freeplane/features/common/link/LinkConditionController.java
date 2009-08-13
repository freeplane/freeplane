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
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public class LinkConditionController implements IElementaryConditionController {
	static final String FILTER_LINK = "filter_link";
	static final String CONNECTOR_LABEL = "connector_label";
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
		return namedObject.objectEquals(FILTER_LINK) || namedObject.objectEquals(CONNECTOR_LABEL);
	}

	public boolean canSelectValues(final Object property, final NamedObject simpleCond) {
		return true;
	}

	public ICondition createCondition(final Object selectedItem, final NamedObject simpleCond, final Object value,
	                                  final boolean ignoreCase) {
		final NamedObject namedObject = (NamedObject) selectedItem;
		if(namedObject.objectEquals(FILTER_LINK)){
			if (simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new HyperLinkEqualsCondition((String) value);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
				return new HyperLinkContainsCondition((String) value);
			}
			return null;
		}
		if(namedObject.objectEquals(CONNECTOR_LABEL)){
			if (simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new ConnectorLabelEqualsCondition((String) value, ignoreCase);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
				return new ConnectorLabelContainsCondition((String) value, ignoreCase);
			}
			return null;
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getLinkConditionNames());
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(ResourceBundles.createTranslatedString(FILTER_LINK));
		list.addElement(ResourceBundles.createTranslatedString(CONNECTOR_LABEL));
		return list;
	}

	private Object[] getLinkConditionNames() {
		return new NamedObject[] { ResourceBundles.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_CONTAINS) };
	}

	public ComboBoxEditor getValueEditor() {
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object property) {
		return values;
	}

	public boolean isCaseDependent(final Object property, final NamedObject simpleCond) {
		return ((NamedObject)property).objectEquals(CONNECTOR_LABEL);
	}

	public ICondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(HyperLinkEqualsCondition.NAME)) {
			final String target = element.getAttribute(HyperLinkEqualsCondition.TEXT, null);
			return new HyperLinkEqualsCondition(target);
		}
		if (element.getName().equalsIgnoreCase(HyperLinkContainsCondition.NAME)) {
			final String target = element.getAttribute(HyperLinkContainsCondition.TEXT, null);
			return new HyperLinkContainsCondition(target);
		}
		if (element.getName().equalsIgnoreCase(ConnectorLabelEqualsCondition.NAME)) {
			final String text = element.getAttribute(ConnectorLabelEqualsCondition.TEXT, null);
			final boolean ignoreCase = Boolean.toString(true).equals(element.getAttribute(ConnectorLabelEqualsCondition.IGNORE_CASE, null));
			return new ConnectorLabelEqualsCondition(text, ignoreCase);
		}
		if (element.getName().equalsIgnoreCase(ConnectorLabelContainsCondition.NAME)) {
			final String text = element.getAttribute(ConnectorLabelContainsCondition.TEXT, null);
			final boolean ignoreCase = Boolean.toString(true).equals(element.getAttribute(ConnectorLabelEqualsCondition.IGNORE_CASE, null));
			return new ConnectorLabelContainsCondition(text, ignoreCase);
		}
		return null;
	}
}
