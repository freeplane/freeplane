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
package org.freeplane.map.icon;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.util.ExtendedComboBoxModel;
import org.freeplane.core.filter.util.IListModel;
import org.freeplane.core.filter.util.SortedMapListModel;
import org.freeplane.core.io.XMLElement;
import org.freeplane.core.map.MindIcon;
import org.freeplane.core.resources.NamedObject;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class IconConditionController implements IElementaryConditionController {
	static final String FILTER_ICON = "filter_icon";

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

	public ICondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                  final Object value, final boolean ignoreCase) {
		return new IconContainedCondition(((MindIcon) value).getName());
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getIconConditionNames());
	}

	public IListModel getFilteredProperties() {
		final SortedMapListModel list = new SortedMapListModel();
		list.add(Controller.getResourceController().createTranslatedString(FILTER_ICON));
		return list;
	}

	public Object[] getIconConditionNames() {
		return new NamedObject[] { Controller.getResourceController().createTranslatedString(
		    ConditionFactory.FILTER_CONTAINS), };
	}

	public ComboBoxEditor getValueEditor() {
		return null;
	}

	public ComboBoxModel getValuesForProperty(final Object property) {
		final SortedMapListModel icons = Controller.getController().getMap().getIconRegistry().getIcons();
		final ExtendedComboBoxModel extendedComboBoxModel = new ExtendedComboBoxModel();
		extendedComboBoxModel.setExtensionList(icons);
		return extendedComboBoxModel;
	}

	public boolean isCaseDependent(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public ICondition loadCondition(final XMLElement element) {
		return IconContainedCondition.load(element);
	}
}
