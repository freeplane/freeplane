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
package org.freeplane.features.common.time;

import java.util.Date;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.util.IListModel;
import org.freeplane.core.filter.util.SortedMapListModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class TimeConditionController implements IElementaryConditionController {
	static final String FILTER_TIME = "filter_time";
	final private Controller controller;
	private final ComboBoxEditor editor = new TimeComboBoxEditor();
	private final ComboBoxModel values = new DefaultComboBoxModel();

	public TimeConditionController(final Controller controller) {
		super();
		this.controller = controller;
	}

	public boolean canEditValues(final Object property, final NamedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(TimeConditionController.FILTER_TIME);
	}

	public boolean canSelectValues(final Object property, final NamedObject simpleCond) {
		return true;
	}

	public ICondition createCondition(final Object selectedItem, final NamedObject simpleCond, final Object value,
	                                  final boolean ignoreCase) {
		return TimeCondition.create(simpleCond, (Date) value);
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getTimeConditionNames());
	}

	public IListModel getFilteredProperties() {
		final SortedMapListModel list = new SortedMapListModel();
		list.add(ResourceBundles.createTranslatedString(FILTER_TIME));
		return list;
	}

	public Object[] getTimeConditionNames() {
		return new NamedObject[] { ResourceBundles.createTranslatedString(TimeCondition.FILTER_MODIFIED_AFTER),
		        ResourceBundles.createTranslatedString(TimeCondition.FILTER_MODIFIED_BEFORE),
		        ResourceBundles.createTranslatedString(TimeCondition.FILTER_CREATED_AFTER),
		        ResourceBundles.createTranslatedString(TimeCondition.FILTER_CREATED_BEFORE) };
	}

	public ComboBoxEditor getValueEditor() {
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem) {
		return values;
	}

	public boolean isCaseDependent(final Object property, final NamedObject simpleCond) {
		return false;
	}

	public ICondition loadCondition(final XMLElement element) {
		try {
			if (element.getName().equalsIgnoreCase(TimeConditionCreatedBefore.NAME)) {
				final String dateString = element.getAttribute("date", null);
				final Date date = new Date(Long.parseLong(dateString));
				return new TimeConditionCreatedBefore(date);
			}
			if (element.getName().equalsIgnoreCase(TimeConditionCreatedAfter.NAME)) {
				final String dateString = element.getAttribute("date", null);
				final Date date = new Date(Long.parseLong(dateString));
				return new TimeConditionCreatedAfter(date);
			}
			if (element.getName().equalsIgnoreCase(TimeConditionModifiedBefore.NAME)) {
				final String dateString = element.getAttribute("date", null);
				final Date date = new Date(Long.parseLong(dateString));
				return new TimeConditionModifiedBefore(date);
			}
			if (element.getName().equalsIgnoreCase(TimeConditionModifiedAfter.NAME)) {
				final String dateString = element.getAttribute("date", null);
				final Date date = new Date(Long.parseLong(dateString));
				return new TimeConditionModifiedAfter(date);
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
