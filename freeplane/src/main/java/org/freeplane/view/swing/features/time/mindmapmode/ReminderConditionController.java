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
package org.freeplane.view.swing.features.time.mindmapmode;

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
import org.freeplane.features.time.TimeComboBoxEditor;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class ReminderConditionController implements IElementaryConditionController {
	static final String FILTER_REMINDER = "filter_reminder";
	private final ComboBoxEditor editor = new TimeComboBoxEditor(true);
	private final ComboBoxModel values = new DefaultComboBoxModel();

	public ReminderConditionController() {
		super();
	}

	public boolean canEditValues(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(ReminderConditionController.FILTER_REMINDER);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
		if(simpleCond.objectEquals(ReminderConditionLater.FILTER_REMINDER_LATER))
			return false;
		if(simpleCond.objectEquals(ReminderConditionExecuted.FILTER_REMINDER_EXECUTED))
			return false;
		return true;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
	                                            final Object value, final boolean matchCase,
	                                            final boolean matchApproximately,
                                                final boolean ignoreDiacritics) {
		return ReminderConditionController.create(simpleCond, (FormattedDate) value);
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getTimeConditionNames());
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_REMINDER));
		return list;
	}

	public Object[] getTimeConditionNames() {
		return new TranslatedObject[] {
		        TextUtils.createTranslatedString(ReminderConditionLater.FILTER_REMINDER_LATER),
		        TextUtils.createTranslatedString(ReminderConditionExecuted.FILTER_REMINDER_EXECUTED),
		        TextUtils.createTranslatedString(ReminderCondition.FILTER_REMINDER_AFTER),
		        TextUtils.createTranslatedString(ReminderCondition.FILTER_REMINDER_BEFORE) };
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem, TranslatedObject simpleCond) {
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
			if (element.getName().equalsIgnoreCase(ReminderConditionLater.NAME)) {
				return new ReminderConditionLater();
			}
			if (element.getName().equalsIgnoreCase(ReminderConditionExecuted.NAME)) {
				return new ReminderConditionExecuted();
			}
			if (element.getName().equalsIgnoreCase(ReminderConditionBefore.NAME)) {
				final String dateString = element.getAttribute(ReminderCondition.DATE, null);
				final FormattedDate date = FormattedDate.createDefaultFormattedDate(Long.parseLong(dateString), IFormattedObject.TYPE_DATETIME);
				return new ReminderConditionBefore(date);
			}
			if (element.getName().equalsIgnoreCase(ReminderConditionAfter.NAME)) {
				final String dateString = element.getAttribute(ReminderCondition.DATE, null);
				final FormattedDate date = FormattedDate.createDefaultFormattedDate(Long.parseLong(dateString), IFormattedObject.TYPE_DATETIME);
				return new ReminderConditionAfter(date);
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

	private static ASelectableCondition create(final TranslatedObject simpleCond, final FormattedDate date) {
    	if (simpleCond.objectEquals(ReminderConditionLater.FILTER_REMINDER_LATER)) {
    		return new ReminderConditionLater();
    	}
    	if (simpleCond.objectEquals(ReminderConditionExecuted.FILTER_REMINDER_EXECUTED)) {
    		return new ReminderConditionExecuted();
    	}
    	if (simpleCond.objectEquals(ReminderCondition.FILTER_REMINDER_AFTER)) {
    		return new ReminderConditionAfter(date);
    	}
    	if (simpleCond.objectEquals(ReminderCondition.FILTER_REMINDER_BEFORE)) {
    		return new ReminderConditionBefore(date);
    	}
    	return null;
    }
}
