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
package org.freeplane.features.icon;

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
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.DefaultConditionRenderer;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class IconConditionController implements IElementaryConditionController {
    static final String FILTER_ICON = "filter_icon";
    static final String FILTER_TAG = "filter_tag";
// // 	final private Controller controller;

	public IconConditionController() {
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
		return namedObject.objectEquals(IconConditionController.FILTER_ICON) || namedObject.objectEquals(IconConditionController.FILTER_TAG);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
	    return !simpleCond.objectEquals(ConditionFactory.FILTER_EXIST);
    }

    public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
                                                final Object value, final boolean matchCase, final boolean approximateMatching,
                                                final boolean ignoreDiacritics) {
        final TranslatedObject namedObject = (TranslatedObject) selectedItem;
        if (namedObject.objectEquals(IconConditionController.FILTER_ICON)) {
            if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS))
                return value instanceof UIIcon ? new IconContainedCondition(((UIIcon) value).getName()) : null;
            if (simpleCond.objectEquals(ConditionFactory.FILTER_EXIST))
                return new IconExistsCondition();
        }
        if (namedObject.objectEquals(IconConditionController.FILTER_TAG)) {
            if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS))
                return value instanceof Tag ? new TagContainedCondition((Tag)value) : null;
        }
        return null;
    }

	public ComboBoxModel getConditionsForProperty(final Object property) {
	    final TranslatedObject namedObject = (TranslatedObject) property;
        if (namedObject.objectEquals(IconConditionController.FILTER_ICON)) {
            return new DefaultComboBoxModel(getIconConditionNames());
        }
        return new DefaultComboBoxModel(getTagConditionNames());
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
        list.addElement(TextUtils.createTranslatedString(FILTER_ICON));
        list.addElement(TextUtils.createTranslatedString(FILTER_TAG));
		return list;
	}

    public Object[] getIconConditionNames() {
        return new TranslatedObject[] {
                TextUtils.createTranslatedString(ConditionFactory.FILTER_CONTAINS),
                TextUtils.createTranslatedString(ConditionFactory.FILTER_EXIST),
        };
    }

    public Object[] getTagConditionNames() {
        return new TranslatedObject[] {
                TextUtils.createTranslatedString(ConditionFactory.FILTER_CONTAINS),
        };
    }

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		return null;
	}

	public ComboBoxModel getValuesForProperty(final Object property, TranslatedObject simpleCond) {
	    final ExtendedComboBoxModel extendedComboBoxModel = new ExtendedComboBoxModel();
	    final TranslatedObject namedObject = (TranslatedObject) property;
	    IconRegistry iconRegistry = Controller.getCurrentController().getMap().getIconRegistry();
	    if (namedObject.objectEquals(IconConditionController.FILTER_ICON)) {
            extendedComboBoxModel.setExtensionList(iconRegistry.getIconsAsListModel());
	    }
	    else {
	        extendedComboBoxModel.setExtensionList(iconRegistry.getTagsAsListModel());
	    }
	    return extendedComboBoxModel;
	}

	public boolean isCaseDependent(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(IconContainedCondition.NAME)) {
			return IconContainedCondition.load(element);
		}
        if (element.getName().equalsIgnoreCase(IconExistsCondition.NAME)) {
            return IconExistsCondition.load(element);
        }
        if (element.getName().equalsIgnoreCase(TagContainedCondition.NAME)) {
            return TagContainedCondition.load(element);
        }
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
		return new DefaultConditionRenderer("", true);
    }
}
