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

import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ConditionFactory;
import org.freeplane.features.common.filter.condition.IElementaryConditionController;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class NodeTextConditionController implements IElementaryConditionController {
	private final ComboBoxModel values = new DefaultComboBoxModel();

	public boolean canEditValues(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof NamedObject)) {
			return false;
		}
		final NamedObject namedObject = (NamedObject) selectedItem;
		return namedObject.objectEquals(TextController.FILTER_NODE)
		|| namedObject.objectEquals(TextController.FILTER_PARENT)
		|| namedObject.objectEquals(TextController.FILTER_DETAILS);
	}

	public boolean canSelectValues(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final NamedObject simpleCondition,
	                                            final Object value, final boolean ignoreCase) {
		final String item = (String) ((NamedObject)selectedItem).getObject();
		return createASelectableCondition(item, simpleCondition, (String) value, ignoreCase);
	}

	private ASelectableCondition createASelectableCondition(final String item, final NamedObject simpleCondition, final String value,
	                                                   final boolean ignoreCase) {
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
			if (value.equals("")) {
				return null;
			}
			// TODO: make ignoreCase a parameter of NodeContainsCondition
			return ignoreCase ? new IgnoreCaseNodeContainsCondition(item, value) : new NodeContainsCondition(item, value);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_REGEXP)) {
			try {
				return new NodeMatchesRegexpCondition(item, value, ignoreCase);
			}
			catch (final PatternSyntaxException e) {
				UITools.errorMessage(TextUtils.format("wrong_regexp", value, e.getMessage()));
				return null;
			}
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
			return new NodeTextCompareCondition(item, value, ignoreCase, 0, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
			return new NodeTextCompareCondition(item, value, ignoreCase, 0, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
			return new NodeTextCompareCondition(item, value, ignoreCase, 1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
			return new NodeTextCompareCondition(item, value, ignoreCase, -1, false);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
			return new NodeTextCompareCondition(item, value, ignoreCase, -1, true);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
			return new NodeTextCompareCondition(item, value, ignoreCase, 1, false);
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new NamedObject[] {
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_CONTAINS),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        NamedObject.literal(ConditionFactory.FILTER_GT), NamedObject.literal(ConditionFactory.FILTER_GE),
		        NamedObject.literal(ConditionFactory.FILTER_LE), NamedObject.literal(ConditionFactory.FILTER_LT),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_REGEXP), });
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(TextController.FILTER_NODE));
		list.addElement(TextUtils.createTranslatedString(TextController.FILTER_PARENT));
		list.addElement(TextUtils.createTranslatedString(TextController.FILTER_DETAILS));
		return list;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, NamedObject selectedCondition) {
		return new BasicComboBoxEditor();
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem, NamedObject simpleCond) {
		return values;
	}

	public boolean isCaseDependent(final Object selectedItem, final NamedObject simpleCond) {
		return true;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(NodeContainsCondition.NAME)) {
			return NodeContainsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(IgnoreCaseNodeContainsCondition.NAME)) {
			return IgnoreCaseNodeContainsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(NodeTextCompareCondition.NAME)) {
			return NodeTextCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(NodeMatchesRegexpCondition.NAME)) {
			return NodeMatchesRegexpCondition.load(element);
		}
		return null;
	}

	static String getItemForComparison(Object nodeItem, final NodeModel node) {
		if(nodeItem.equals(TextController.FILTER_NODE)){
			return TextController.getController().getPlainTextContent(node);
		}
		if(nodeItem.equals(TextController.FILTER_PARENT)){
			final NodeModel parentNode = node.getParentNode();
			if(parentNode == null){
				return null;
			}
			return TextController.getController().getPlainTextContent(parentNode);
		}
		if(nodeItem.equals(TextController.FILTER_DETAILS)){
			final String html = DetailTextModel.getDetailTextText(node);
			if(html == null){
				return null;
			}
			return HtmlUtils.htmlToPlain(html);
		}
		return null;
    }
}
