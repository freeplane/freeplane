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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.util.collection.ExtendedComboBoxModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class IconConditionController implements IElementaryConditionController {
	static final String FILTER_ICON = "filter_icon";
	final private Controller controller;

	public IconConditionController(final Controller controller) {
		super();
		this.controller = controller;
	}

	public boolean canEditValues(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(IconConditionController.FILTER_ICON);
	}

	public boolean canSelectValues(final Object property, final NamedObject simpleCond) {
		return true;
	}

	public ISelectableCondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                            final Object value, final boolean ignoreCase) {
		return value instanceof UIIcon ? new IconContainedCondition(((UIIcon) value).getName()) : null;
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getIconConditionNames());
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(ResourceBundles.createTranslatedString(FILTER_ICON));
		return list;
	}

	public Object[] getIconConditionNames() {
		return new NamedObject[] { ResourceBundles.createTranslatedString(ConditionFactory.FILTER_CONTAINS), };
	}

	public ComboBoxEditor getValueEditor() {
		return null;
	}

	public ComboBoxModel getValuesForProperty(final Object property) {
		final ListModel icons = controller.getMap().getIconRegistry().getIconsAsListModel();
		final ExtendedComboBoxModel extendedComboBoxModel = new ExtendedComboBoxModel();
		extendedComboBoxModel.setExtensionList(icons);
		return extendedComboBoxModel;
	}

	public boolean isCaseDependent(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public ISelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(IconContainedCondition.NAME)) {
			return IconContainedCondition.load(element);
		}
		return null;
	}
}
