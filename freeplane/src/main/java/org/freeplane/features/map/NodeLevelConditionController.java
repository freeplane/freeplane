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
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 27.07.2010
 */
class NodeLevelConditionController implements IElementaryConditionController {
	static final String FILTER_LEVEL = "filter_node_level";
	static final String FILTER_ROOT = "filter_root";
	static final String FILTER_LEAF = "filter_leaf";
	static final String FILTER_PERIODIC_LEVEL = "filter_periodic_level";
	private ComboBoxEditor levelEditor;
	private ComboBoxModel values;
	private ComboBoxModel periodicValues;


	public boolean canEditValues(final Object selectedItem, final TranslatedObject simpleCond) {
		return ! simpleCond.objectEquals(FILTER_PERIODIC_LEVEL);
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		if (namedObject.objectEquals(NodeLevelConditionController.FILTER_LEVEL))
			return true;
		return false;
	}

	public boolean canSelectValues(final Object selectedItem, final TranslatedObject simpleCondition) {
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
			return true;
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
			return true;
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
			return true;
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
			return true;
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
			return true;
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
			return true;
		}
		if (simpleCondition.objectEquals(FILTER_PERIODIC_LEVEL)) {
			return true;
		}
		
		return false;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
	                                            final Object value, final boolean matchCase,
	                                            final boolean matchApproximately,
                                                final boolean ignoreDiacritics) {
		if(value instanceof PeriodicLevelCondition){
			return (ASelectableCondition) value;
		}
		return createASelectableCondition(simpleCond, (String) value, matchCase, matchApproximately, ignoreDiacritics);
	}

	protected ASelectableCondition createASelectableCondition(final TranslatedObject simpleCondition, final String value,
	                                                   final boolean matchCase, final boolean matchApproximately,
	                                                   final boolean ignoreDiacritics) {
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
			return new NodeLevelCompareCondition(value, matchCase, 0, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
			return new NodeLevelCompareCondition(value, matchCase, 0, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
			return new NodeLevelCompareCondition(value, matchCase, 1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
			return new NodeLevelCompareCondition(value, matchCase, -1, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
			return new NodeLevelCompareCondition(value, matchCase, -1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
			return new NodeLevelCompareCondition(value, matchCase, 1, false);
		}
		if (simpleCondition.objectEquals(NodeLevelConditionController.FILTER_ROOT))
			return new RootCondition();
		if (simpleCondition.objectEquals(NodeLevelConditionController.FILTER_LEAF))
			return new LeafCondition();
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new TranslatedObject[] {
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        TranslatedObject.literal(ConditionFactory.FILTER_GT), TranslatedObject.literal(ConditionFactory.FILTER_GE),
		        TranslatedObject.literal(ConditionFactory.FILTER_LE), TranslatedObject.literal(ConditionFactory.FILTER_LT),
		        TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_ROOT),
		        TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_LEAF),
		        TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_PERIODIC_LEVEL),
		        });
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_LEVEL));
		return list;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		if(selectedCondition.objectEquals(FILTER_PERIODIC_LEVEL)){
			return new FixedBasicComboBoxEditor();
		}
		if(levelEditor == null)
			levelEditor = new NumberComboBoxEditor();

		return levelEditor;
	}

	public ComboBoxModel getValuesForProperty(final Object property, TranslatedObject simpleCond) {
		if(simpleCond.objectEquals(FILTER_PERIODIC_LEVEL)){
			if(periodicValues == null)
				periodicValues = new DefaultComboBoxModel(PeriodicLevelCondition.createConditions(7));
			return periodicValues;
		}
		if(values == null)
			values = new DefaultComboBoxModel();
		return values;
	}

	public boolean isCaseDependent(final Object selectedItem, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean supportsApproximateMatching(final Object selectedItem, final TranslatedObject simpleCond) {
		return false;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(NodeLevelCompareCondition.NAME)) {
			return NodeLevelCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(RootCondition.NAME)) {
			return RootCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(LeafCondition.NAME)) {
			return LeafCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(PeriodicLevelCondition.NAME)) {
			return PeriodicLevelCondition.load(element);
		}
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
	    return null;
    }
}
