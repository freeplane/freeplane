/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.explorer;

import java.util.Collection;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.ExtendedComboBoxModel;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.DefaultConditionRenderer;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public class MapExplorerConditionController implements IElementaryConditionController {
	static final String FILTER_GLOBAL = "filter_global";
	static final String FILTER_ALIAS = "filter_alias";
	private final ExtendedComboBoxModel values = new ExtendedComboBoxModel();

	public boolean canEditValues(final Object property, final TranslatedObject simpleCond) {
		return canSelectValues(property, simpleCond);
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(FILTER_GLOBAL) || namedObject.objectEquals(FILTER_ALIAS);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
		return ((TranslatedObject)property).objectEquals(FILTER_ALIAS) && !simpleCond.objectEquals(ConditionFactory.FILTER_EXIST);
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
	                                            final Object value, final boolean matchCase,
	                                            final boolean matchApproximately,
                                                final boolean ignoreDiacritics) {
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		if (namedObject.objectEquals(FILTER_GLOBAL)) {
			return new GlobalNodeCondition();
		}
		if (namedObject.objectEquals(FILTER_ALIAS)) {
			if (simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new AliasEqualsCondition((String) value, matchCase, matchApproximately, ignoreDiacritics);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_STARTS_WITH)) {
				return new AliasStartsWithCondition((String) value, matchCase, matchApproximately, ignoreDiacritics);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_EXIST)) {
				return new AliasExistsCondition();
			}
			return null;
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		final TranslatedObject no = (TranslatedObject) property;
		final Object[] conditions;
		if (no.getObject().equals(FILTER_GLOBAL)) {
			conditions = new TranslatedObject[] {TranslatedObject.literal("")};
		}
		else if (no.getObject().equals(FILTER_ALIAS)) {
			conditions = new TranslatedObject[] {
			        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
			        TextUtils.createTranslatedString(ConditionFactory.FILTER_STARTS_WITH),
			        TextUtils.createTranslatedString(ConditionFactory.FILTER_EXIST) };
		}
		else throw new IllegalArgumentException();
		return new DefaultComboBoxModel(conditions);
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_ALIAS));
		list.addElement(TextUtils.createTranslatedString(FILTER_GLOBAL));
		return list;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		return new FixedBasicComboBoxEditor();
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem, TranslatedObject simpleCond) {
		if(canSelectValues(selectedItem, simpleCond))
			values.setExtensionList(aliases());
		else
			values.setExtensionList(null);
		return values;
	}

	private SortedComboBoxModel aliases() {
		final MapModel map = Controller.getCurrentController().getMap();
		Collection<NodeAlias> aliases = NodeAliases.of(map).aliases();
		SortedComboBoxModel box = new SortedComboBoxModel();
		for(NodeAlias a : aliases)
			box.add(a.value);
		return box;
	}
	
	public boolean isCaseDependent(final Object property, final TranslatedObject simpleCond) {
		return true;
	}
	
	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(AliasEqualsCondition.NAME)) {
			final String target = element.getAttribute(AliasEqualsCondition.TEXT, null);
			final boolean matchCase = Boolean.toString(true).equals(
				    element.getAttribute(StringConditionAdapter.MATCH_CASE, null));
			final boolean matchApproximately = Boolean.toString(true).equals(
					element.getAttribute(StringConditionAdapter.MATCH_APPROXIMATELY, null));
			return new AliasEqualsCondition(target, matchCase, matchApproximately,
			        Boolean.valueOf(element.getAttribute(StringConditionAdapter.IGNORE_DIACRITICS, null)));
		}
		if (element.getName().equalsIgnoreCase(AliasStartsWithCondition.NAME)) {
			final String target = element.getAttribute(AliasEqualsCondition.TEXT, null);
			final boolean matchCase = Boolean.toString(true).equals(
				    element.getAttribute(AliasEqualsCondition.MATCH_CASE, null));
			final boolean matchApproximately = Boolean.toString(true).equals(
					element.getAttribute(AliasEqualsCondition.MATCH_APPROXIMATELY, null));
			return new AliasStartsWithCondition(target, matchCase, matchApproximately,
			        Boolean.valueOf(element.getAttribute(StringConditionAdapter.IGNORE_DIACRITICS, null)));
		}
		if (element.getName().equalsIgnoreCase(AliasExistsCondition.NAME)) {
			return new AliasExistsCondition();
		}
		if (element.getName().equalsIgnoreCase(GlobalNodeCondition.NAME)) {
			return new GlobalNodeCondition();
		}
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
		if (((TranslatedObject)selectedProperty).objectEquals(FILTER_GLOBAL) ||
			(((TranslatedObject)selectedProperty).objectEquals(FILTER_ALIAS) &&
					selectedCondition.objectEquals(ConditionFactory.FILTER_EXIST)))
		{
			// don't return null as this would make FilterConditionEditor fall back to filterController.getConditionRenderer()
			// (and that would put in a default string like "No Filtering (remove)"!)
			return new DefaultConditionRenderer("", true);
		}
		else
		{
			return null;
		}
    }

	public static void installFilterConditions() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(100, new MapExplorerConditionController());
	}
}
