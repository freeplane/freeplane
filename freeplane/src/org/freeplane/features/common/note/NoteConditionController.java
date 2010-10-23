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
package org.freeplane.features.common.note;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ConditionFactory;
import org.freeplane.features.common.filter.condition.IElementaryConditionController;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class NoteConditionController implements IElementaryConditionController {
	static final String FILTER_NOTE = "filter_note";
	private final ComboBoxEditor editor = new BasicComboBoxEditor();
	private final ComboBoxModel values = new DefaultComboBoxModel();

	public boolean canEditValues(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(NoteConditionController.FILTER_NOTE);
	}

	public boolean canSelectValues(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                            final Object value, final boolean ignoreCase) {
		return createASelectableCondition(simpleCond, (String) value, ignoreCase);
	}

	protected ASelectableCondition createASelectableCondition(final NamedObject simpleCondition, final String value,
	                                                   final boolean ignoreCase) {
		if (ignoreCase) {
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
				if (value.equals("")) {
					return null;
				}
				return new IgnoreCaseNoteContainsCondition(value);
			}
		}
		else {
			if (simpleCondition.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
				if (value.equals("")) {
					return null;
				}
				return new NoteContainsCondition(value);
			}
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new NamedObject[] { TextUtils
		    .createTranslatedString(ConditionFactory.FILTER_CONTAINS) });
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(NoteConditionController.FILTER_NOTE));
		return list;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, NamedObject selectedCondition) {
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem, NamedObject simpleCond) {
		return values;
	}

	public boolean isCaseDependent(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(NoteContainsCondition.NAME)) {
			return NoteContainsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(IgnoreCaseNoteContainsCondition.NAME)) {
			return IgnoreCaseNoteContainsCondition.load(element);
		}
		return null;
	}
}
