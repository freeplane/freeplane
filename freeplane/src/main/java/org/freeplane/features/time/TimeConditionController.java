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
package org.freeplane.features.time;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
public class TimeConditionController implements IElementaryConditionController {
	static final String FILTER_TIME = "filter_time";
// // //	final private Controller controller;
	private ComboBoxEditor editor = null;
	private ComboBoxModel values = null;

	public TimeConditionController() {
		super();
//		this.controller = controller;
	}

	public boolean canEditValues(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(TimeConditionController.FILTER_TIME);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
	                                            final Object value, final boolean matchCase,
	                                            final boolean matchApproximately) {
		return TimeCondition.create(simpleCond, (FormattedDate) value);
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getTimeConditionNames());
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_TIME));
		return list;
	}

	public Object[] getTimeConditionNames() {
		return new TranslatedObject[] { TextUtils.createTranslatedString(TimeCondition.FILTER_MODIFIED_AFTER),
		        TextUtils.createTranslatedString(TimeCondition.FILTER_MODIFIED_BEFORE),
		        TextUtils.createTranslatedString(TimeCondition.FILTER_CREATED_AFTER),
		        TextUtils.createTranslatedString(TimeCondition.FILTER_CREATED_BEFORE) };
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		if(editor == null)
			editor = new TimeComboBoxEditor(true);
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem, TranslatedObject simpleCond) {
		if(values == null)
			values = new DefaultComboBoxModel();
		values.setSelectedItem(FormattedDate.createDefaultFormattedDate(System.currentTimeMillis(), IFormattedObject.TYPE_DATETIME));
		return values;
	}

	public boolean isCaseDependent(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		try {
			if (element.getName().equalsIgnoreCase(TimeConditionCreatedBefore.NAME)) {
				final String dateString = element.getAttribute(TimeCondition.DATE, null);
				FormattedDate date  = FormattedDate.createDefaultFormattedDate(Long.parseLong(dateString), IFormattedObject.TYPE_DATETIME);
				return new TimeConditionCreatedBefore(date);
			}
			if (element.getName().equalsIgnoreCase(TimeConditionCreatedAfter.NAME)) {
				final String dateString = element.getAttribute(TimeCondition.DATE, null);
				FormattedDate date  = FormattedDate.createDefaultFormattedDate(Long.parseLong(dateString), IFormattedObject.TYPE_DATETIME);
				return new TimeConditionCreatedAfter(date);
			}
			if (element.getName().equalsIgnoreCase(TimeConditionModifiedBefore.NAME)) {
				final String dateString = element.getAttribute(TimeCondition.DATE, null);
				FormattedDate date  = FormattedDate.createDefaultFormattedDate(Long.parseLong(dateString), IFormattedObject.TYPE_DATETIME);
				return new TimeConditionModifiedBefore(date);
			}
			if (element.getName().equalsIgnoreCase(TimeConditionModifiedAfter.NAME)) {
				final String dateString = element.getAttribute(TimeCondition.DATE, null);
				FormattedDate date  = FormattedDate.createDefaultFormattedDate(Long.parseLong(dateString), IFormattedObject.TYPE_DATETIME);
				return new TimeConditionModifiedAfter(date);
			}
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
	    return null;
    }
}
