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
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.condition.ISelectableCondition;
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
	static final String CONNECTOR = "connector";
	private final ComboBoxEditor editor = new BasicComboBoxEditor();
	private final ComboBoxModel values = new DefaultComboBoxModel();

	public boolean canEditValues(final Object property, final NamedObject simpleCond) {
		return !simpleCond.objectEquals(ConditionFactory.FILTER_EXIST);
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(FILTER_LINK) || namedObject.objectEquals(CONNECTOR_LABEL)
		        || namedObject.objectEquals(CONNECTOR);
	}

	public boolean canSelectValues(final Object property, final NamedObject simpleCond) {
		return !simpleCond.objectEquals(ConditionFactory.FILTER_EXIST);
	}

	public ISelectableCondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                            final Object value, final boolean ignoreCase) {
		final NamedObject namedObject = (NamedObject) selectedItem;
		if (namedObject.objectEquals(FILTER_LINK)) {
			if (simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new HyperLinkEqualsCondition((String) value);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
				return new HyperLinkContainsCondition((String) value);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_EXIST)) {
				return new HyperLinkExistsCondition();
			}
			return null;
		}
		if (namedObject.objectEquals(CONNECTOR_LABEL)) {
			if (simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new ConnectorLabelEqualsCondition((String) value, ignoreCase);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
				return new ConnectorLabelContainsCondition((String) value, ignoreCase);
			}
			return null;
		}
		if (namedObject.objectEquals(CONNECTOR)) {
			if (simpleCond.objectEquals(ConditionFactory.FILTER_EXIST)) {
				return new ConnectorExistsCondition();
			}
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		final NamedObject no = (NamedObject) property;
		final Object[] linkConditionNames;
		if (no.getObject().equals(FILTER_LINK)) {
			linkConditionNames = new NamedObject[] {
			        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
			        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_CONTAINS),
			        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_EXIST) };
		}
		else if (no.getObject().equals(CONNECTOR_LABEL)) {
			linkConditionNames = new NamedObject[] {
			        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
			        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_CONTAINS) };
		}
		else {
			linkConditionNames = new NamedObject[] { ResourceBundles
			    .createTranslatedString(ConditionFactory.FILTER_EXIST) };
		}
		return new DefaultComboBoxModel(linkConditionNames);
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(ResourceBundles.createTranslatedString(FILTER_LINK));
		list.addElement(ResourceBundles.createTranslatedString(CONNECTOR_LABEL));
		list.addElement(ResourceBundles.createTranslatedString(CONNECTOR));
		return list;
	}

	public ComboBoxEditor getValueEditor() {
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object property) {
		return values;
	}

	public boolean isCaseDependent(final Object property, final NamedObject simpleCond) {
		return ((NamedObject) property).objectEquals(CONNECTOR_LABEL);
	}

	public ISelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(HyperLinkEqualsCondition.NAME)) {
			final String target = element.getAttribute(HyperLinkEqualsCondition.TEXT, null);
			return new HyperLinkEqualsCondition(target);
		}
		if (element.getName().equalsIgnoreCase(HyperLinkContainsCondition.NAME)) {
			final String target = element.getAttribute(HyperLinkContainsCondition.TEXT, null);
			return new HyperLinkContainsCondition(target);
		}
		if (element.getName().equalsIgnoreCase(HyperLinkExistsCondition.NAME)) {
			return new HyperLinkExistsCondition();
		}
		if (element.getName().equalsIgnoreCase(ConnectorLabelEqualsCondition.NAME)) {
			final String text = element.getAttribute(ConnectorLabelEqualsCondition.TEXT, null);
			final boolean ignoreCase = Boolean.toString(true).equals(
			    element.getAttribute(ConnectorLabelEqualsCondition.IGNORE_CASE, null));
			return new ConnectorLabelEqualsCondition(text, ignoreCase);
		}
		if (element.getName().equalsIgnoreCase(ConnectorLabelContainsCondition.NAME)) {
			final String text = element.getAttribute(ConnectorLabelContainsCondition.TEXT, null);
			final boolean ignoreCase = Boolean.toString(true).equals(
			    element.getAttribute(ConnectorLabelEqualsCondition.IGNORE_CASE, null));
			return new ConnectorLabelContainsCondition(text, ignoreCase);
		}
		if (element.getName().equalsIgnoreCase(ConnectorExistsCondition.NAME)) {
			return new ConnectorExistsCondition();
		}
		return null;
	}
}
