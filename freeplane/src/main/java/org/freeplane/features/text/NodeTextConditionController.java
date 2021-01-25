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
package org.freeplane.features.text;

import java.util.regex.PatternSyntaxException;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.ui.components.TypedListCellRenderer;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.ui.FrameController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class NodeTextConditionController implements IElementaryConditionController {
	private final ComboBoxModel values = new DefaultComboBoxModel();

	public boolean canEditValues(final Object selectedItem, final TranslatedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(TextController.FILTER_NODE)
		|| namedObject.objectEquals(TextController.FILTER_PARENT)
		|| namedObject.objectEquals(TextController.FILTER_DETAILS)
		|| namedObject.objectEquals(TextController.FILTER_NOTE)
		|| namedObject.objectEquals(TextController.FILTER_ANYTEXT);
	}

	public boolean canSelectValues(final Object selectedItem, final TranslatedObject simpleCond) {
		return true;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCondition,
	                                            final Object value, final boolean matchCase, final boolean matchApproximately,
                                                final boolean ignoreDiacritics) {
		final String item = (String) ((TranslatedObject)selectedItem).getObject();
		return createASelectableCondition(item, simpleCondition, value, matchCase, matchApproximately, ignoreDiacritics);
	}

	private ASelectableCondition createASelectableCondition(final String item, final TranslatedObject simpleCondition, final Object value,
	                                                   final boolean matchCase, final boolean matchApproximately,
	                                                   final boolean ignoreDiacritics) {
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
			if (value.equals("")) {
				return null;
			}
			return new NodeContainsCondition(item, value.toString(), matchCase, matchApproximately, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_REGEXP)) {
			try {
				return new NodeMatchesRegexpCondition(item, value.toString(), matchCase);
			}
			catch (final PatternSyntaxException e) {
				UITools.errorMessage(TextUtils.format("wrong_regexp", value, e.getMessage()));
				return null;
			}
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
			return new NodeTextCompareCondition(item, value, matchCase, 0, true, matchApproximately, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
			return new NodeTextCompareCondition(item, value, matchCase, 0, false, matchApproximately, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GT)) {
			return new NodeTextCompareCondition(item, value, matchCase, 1, true, matchApproximately, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_GE)) {
			return new NodeTextCompareCondition(item, value, matchCase, -1, false, matchApproximately, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LT)) {
			return new NodeTextCompareCondition(item, value, matchCase, -1, true, matchApproximately, ignoreDiacritics);
		}
		if (simpleCondition.objectEquals(ConditionFactory.FILTER_LE)) {
			return new NodeTextCompareCondition(item, value, matchCase, 1, false, matchApproximately, ignoreDiacritics);
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object selectedItem) {
		return new DefaultComboBoxModel(new TranslatedObject[] {
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_CONTAINS),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        TranslatedObject.literal(ConditionFactory.FILTER_GT), TranslatedObject.literal(ConditionFactory.FILTER_GE),
		        TranslatedObject.literal(ConditionFactory.FILTER_LE), TranslatedObject.literal(ConditionFactory.FILTER_LT),
		        TextUtils.createTranslatedString(ConditionFactory.FILTER_REGEXP), });
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(TextController.FILTER_ANYTEXT));
		list.addElement(TextUtils.createTranslatedString(TextController.FILTER_NODE));
		list.addElement(TextUtils.createTranslatedString(TextController.FILTER_DETAILS));
		list.addElement(TextUtils.createTranslatedString(TextController.FILTER_NOTE));
		list.addElement(TextUtils.createTranslatedString(TextController.FILTER_PARENT));
		return list;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		if(selectedCondition.objectEquals(ConditionFactory.FILTER_CONTAINS) 
				|| selectedCondition.objectEquals(ConditionFactory.FILTER_REGEXP) )
			return new FixedBasicComboBoxEditor();
		return FrameController.getTextDateTimeEditor();
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem, TranslatedObject simpleCond) {
		return values;
	}

	public boolean isCaseDependent(final Object selectedItem, final TranslatedObject simpleCond) {
		return true;
	}
	
	public boolean supportsApproximateMatching(final Object selectedItem, final TranslatedObject simpleCond) {
		return true;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(NodeContainsCondition.IGNORE_CASE_NAME)) {
			return NodeContainsCondition.loadIgnoreCase(element);
		}
		if (element.getName().equalsIgnoreCase(NodeContainsCondition.MATCH_CASE_NAME)) {
			return NodeContainsCondition.loadMatchCase(element);
		}
		if (element.getName().equalsIgnoreCase(NodeTextCompareCondition.NAME)) {
			return NodeTextCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(NodeMatchesRegexpCondition.NAME)) {
			return NodeMatchesRegexpCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(NoteContainsCondition.IGNORE_CASE_NAME)) {
			return NoteContainsCondition.loadIgnoreCase(element);
		}
		if (element.getName().equalsIgnoreCase(NoteContainsCondition.MATCH_CASE_NAME)) {
			return NoteContainsCondition.loadMatchCase(element);
		}
		return null;
	}

	public static Object[] getItemsForComparison(Object nodeItem, final NodeModel node) {
		if (nodeItem.equals(TextController.FILTER_ANYTEXT)) {
			return new Object[] { 
					getItemForComparison(TextController.FILTER_NODE, node), 
					getItemForComparison(TextController.FILTER_DETAILS, node),
			        getItemForComparison(TextController.FILTER_NOTE, node) };
		}
		else
			return new Object[] { getItemForComparison(nodeItem, node) };
	}
	
	private static Object getItemForComparison(Object nodeItem, final NodeModel node) {
		final Object result;
		if(nodeItem.equals(TextController.FILTER_NODE)){
			result = transformedObject(node);
		}
		else if(nodeItem.equals(TextController.FILTER_PARENT)){
			final NodeModel parentNode = node.getParentNode();
			if(parentNode == null)
				result = null;
			else
				result = transformedObject(parentNode);
		}
		else if(nodeItem.equals(TextController.FILTER_DETAILS)){
			result = DetailModel.getDetailText(node);
		}
		else if(nodeItem.equals(TextController.FILTER_NOTE)){
			result = NoteModel.getNoteText(node);
		}
		else
			result = null;
		if(result instanceof String)
			return HtmlUtils.htmlToPlain((String)result);
		return result;
    }

	private static Object transformedObject(final NodeModel node) {
		final Object userObject = node.getUserObject();
		return TextController.getController().getTransformedObjectNoFormattingNoThrow(node, node, userObject);
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
        if(selectedCondition.objectEquals(ConditionFactory.FILTER_CONTAINS) 
                || selectedCondition.objectEquals(ConditionFactory.FILTER_REGEXP) )
            return null;
	    return new TypedListCellRenderer();
    }
}
