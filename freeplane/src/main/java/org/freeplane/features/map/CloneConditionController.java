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
package org.freeplane.features.map;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.ExtendedComboBoxModel;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.DefaultConditionRenderer;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class CloneConditionController implements IElementaryConditionController {
	static final String FILTER_CLONE = "filter_clones";
// // 	final private Controller controller;

	public CloneConditionController() {
		super();
//		this.controller = controller;
	}

	public boolean canEditValues(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(CloneConditionController.FILTER_CLONE);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
	    return false;
    }

    public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
                                                final Object value, final boolean matchCase, final boolean approximateMatching) {
        return new CloneOfSelectedViewCondition();
    }

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getCloneConditionNames());
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_CLONE));
		return list;
	}

	public Object[] getCloneConditionNames() {
		return new TranslatedObject[] { 
				TranslatedObject.literal(""), 
		};
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		return null;
	}

	public ComboBoxModel getValuesForProperty(final Object property, TranslatedObject simpleCond) {
	    final ListModel icons = Controller.getCurrentController().getMap().getIconRegistry().getIconsAsListModel();
	    final ExtendedComboBoxModel extendedComboBoxModel = new ExtendedComboBoxModel();
	    extendedComboBoxModel.setExtensionList(icons);
	    return extendedComboBoxModel;
	}

	public boolean isCaseDependent(final Object property, final TranslatedObject simpleCond) {
		return false;
	}
	
	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
		return new DefaultConditionRenderer("", true);
    }
}
