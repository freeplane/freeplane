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
package org.freeplane.features.common.map;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ConditionFactory;
import org.freeplane.features.common.filter.condition.IElementaryConditionController;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 27.07.2010
 */
class NodeLevelConditionController implements IElementaryConditionController {
	static final String FILTER_LEVEL = "filter_node_level";
	static final String FILTER_ROOT = "filter_root";
	static final String FILTER_LEAF = "filter_leaf";
	static final String FILTER_ODD_LEVEL = "filter_odd_level";
	static final String FILTER_EVEN_LEVEL = "filter_even_level";
	private final ComboBoxEditor levelEditor = new NumberComboBoxEditor();
	private final ComboBoxModel values = new DefaultComboBoxModel();

	public boolean canEditValues(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		if (namedObject.objectEquals(NodeLevelConditionController.FILTER_LEVEL))
			return true;
		return false;
	}

	public boolean canSelectValues(final Object selectedItem, final NamedObject simpleCondition) {
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
		return false;
	}

	public ISelectableCondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                            final Object value, final boolean ignoreCase) {
		return createNodeCondition(simpleCond, (String) value, ignoreCase);
	}

	protected ISelectableCondition createNodeCondition(final NamedObject simpleCondition, final String value,
	                                                   final boolean ignoreCase) {
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
			return new NodeLevelCompareCondition(value, ignoreCase, 0, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
			return new NodeLevelCompareCondition(value, ignoreCase, 0, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
			return new NodeLevelCompareCondition(value, ignoreCase, 1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
			return new NodeLevelCompareCondition(value, ignoreCase, -1, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
			return new NodeLevelCompareCondition(value, ignoreCase, -1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
			return new NodeLevelCompareCondition(value, ignoreCase, 1, false);
		}
		if (simpleCondition.objectEquals(NodeLevelConditionController.FILTER_ROOT))
			return new RootCondition();
		if (simpleCondition.objectEquals(NodeLevelConditionController.FILTER_LEAF))
			return new LeafCondition();
		if (simpleCondition.objectEquals(NodeLevelConditionController.FILTER_ODD_LEVEL))
			return new OddLevelCondition();
		if (simpleCondition.objectEquals(NodeLevelConditionController.FILTER_EVEN_LEVEL))
			return new EvenLevelCondition();
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new NamedObject[] {
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        NamedObject.literal(ConditionFactory.FILTER_GT), NamedObject.literal(ConditionFactory.FILTER_GE),
		        NamedObject.literal(ConditionFactory.FILTER_LE), NamedObject.literal(ConditionFactory.FILTER_LT),
		        TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_ROOT),
		        TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_LEAF),
		        TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_ODD_LEVEL),
		        TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_EVEN_LEVEL)
		        });
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(NodeLevelConditionController.FILTER_LEVEL));
		return list;
	}

	public ComboBoxEditor getValueEditor() {
		return levelEditor;
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem) {
		return values;
	}

	public boolean isCaseDependent(final Object selectedItem, final NamedObject simpleCond) {
		return false;
	}

	public ISelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(NodeLevelCompareCondition.NAME)) {
			return NodeLevelCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(RootCondition.NAME)) {
			return RootCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(LeafCondition.NAME)) {
			return LeafCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(EvenLevelCondition.NAME)) {
			return EvenLevelCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(OddLevelCondition.NAME)) {
			return OddLevelCondition.load(element);
		}
		return null;
	}
}
