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
package org.freeplane.features.common.text;

import java.util.regex.PatternSyntaxException;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class NodeConditionController implements IElementaryConditionController {
	static final String FILTER_NODE = "filter_node";
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
		return namedObject.objectEquals(NodeConditionController.FILTER_NODE);
	}

	public boolean canSelectValues(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public ISelectableCondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                            final Object value, final boolean ignoreCase) {
		return createNodeCondition(simpleCond, (String) value, ignoreCase);
	}

	protected ISelectableCondition createNodeCondition(final NamedObject simpleCondition, final String value,
	                                                   final boolean ignoreCase) {
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
			if (value.equals("")) {
				return null;
			}
			// TODO: make ignoreCase a parameter of NodeContainsCondition
			return ignoreCase ? new IgnoreCaseNodeContainsCondition(value) : new NodeContainsCondition(value);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_REGEXP)) {
			try {
				return new NodeMatchesRegexpCondition(value, ignoreCase);
			} catch (PatternSyntaxException e) {
				UITools.errorMessage(FpStringUtils.format("wrong_regexp", value, e.getMessage()));
				return null;
			}
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
			return new NodeCompareCondition(value, ignoreCase, 0, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
			return new NodeCompareCondition(value, ignoreCase, 0, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
			return new NodeCompareCondition(value, ignoreCase, 1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
			return new NodeCompareCondition(value, ignoreCase, -1, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
			return new NodeCompareCondition(value, ignoreCase, -1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
			return new NodeCompareCondition(value, ignoreCase, 1, false);
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new NamedObject[] {
		        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_CONTAINS),
		        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        NamedObject.literal(ConditionFactory.FILTER_GT), NamedObject.literal(ConditionFactory.FILTER_GE),
		        NamedObject.literal(ConditionFactory.FILTER_LE), NamedObject.literal(ConditionFactory.FILTER_LT),
		        ResourceBundles.createTranslatedString(ConditionFactory.FILTER_REGEXP), });
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(ResourceBundles.createTranslatedString(NodeConditionController.FILTER_NODE));
		return list;
	}

	public ComboBoxEditor getValueEditor() {
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem) {
		return values;
	}

	public boolean isCaseDependent(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public ISelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(NodeContainsCondition.NAME)) {
			return NodeContainsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(IgnoreCaseNodeContainsCondition.NAME)) {
			return IgnoreCaseNodeContainsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(NodeCompareCondition.NAME)) {
			return NodeCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(NodeMatchesRegexpCondition.NAME)) {
			return NodeMatchesRegexpCondition.load(element);
		}
		return null;
	}
}
