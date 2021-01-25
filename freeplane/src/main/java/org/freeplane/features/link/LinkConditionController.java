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
package org.freeplane.features.link;

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
import org.freeplane.features.filter.condition.DefaultConditionRenderer;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public class LinkConditionController implements IElementaryConditionController {
	static final String FILTER_LINK = "filter_link";
	static final String CONNECTOR_LABEL = "connector_label";
	static final String CONNECTOR = "connector";
	private final DefaultComboBoxModel values = new DefaultComboBoxModel();

	public boolean canEditValues(final Object property, final TranslatedObject simpleCond) {
		return ! (simpleCond.objectEquals(ConditionFactory.FILTER_EXIST)
		        || simpleCond.objectEquals(ConnectorStyleCondition.FILTER_STYLE));
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(FILTER_LINK) || namedObject.objectEquals(CONNECTOR_LABEL)
		        || namedObject.objectEquals(CONNECTOR);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
		return !simpleCond.objectEquals(ConditionFactory.FILTER_EXIST);
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
	                                            final Object value, final boolean matchCase,
	                                            final boolean matchApproximately,
                                                final boolean ignoreDiacritics) {
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		if (namedObject.objectEquals(FILTER_LINK)) {
			if (simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new HyperLinkEqualsCondition((String) value, matchCase, matchApproximately, ignoreDiacritics);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
				return new HyperLinkContainsCondition((String) value, matchCase, matchApproximately, ignoreDiacritics);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_EXIST)) {
				return new HyperLinkExistsCondition();
			}
			return null;
		}
		if (namedObject.objectEquals(CONNECTOR_LABEL)) {
			if (simpleCond.objectEquals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new ConnectorLabelEqualsCondition((String) value, matchCase, matchApproximately, ignoreDiacritics);
			}
			if (simpleCond.objectEquals(ConditionFactory.FILTER_CONTAINS)) {
				return new ConnectorLabelContainsCondition((String) value, matchCase, matchApproximately, ignoreDiacritics);
			}
			return null;
		}
		if (namedObject.objectEquals(CONNECTOR)) {
            if (simpleCond.objectEquals(ConditionFactory.FILTER_EXIST)) {
                return new ConnectorExistsCondition();
            }
            if (simpleCond.objectEquals(ConnectorStyleCondition.FILTER_STYLE)) {
                return new ConnectorStyleCondition((IStyle) value);
            }
		}
		return null;
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		final TranslatedObject no = (TranslatedObject) property;
		final Object[] linkConditionNames;
		if (no.getObject().equals(FILTER_LINK)) {
			linkConditionNames = new TranslatedObject[] {
			        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
			        TextUtils.createTranslatedString(ConditionFactory.FILTER_CONTAINS),
			        TextUtils.createTranslatedString(ConditionFactory.FILTER_EXIST) };
		}
		else if (no.getObject().equals(CONNECTOR_LABEL)) {
			linkConditionNames = new TranslatedObject[] {
			        TextUtils.createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
			        TextUtils.createTranslatedString(ConditionFactory.FILTER_CONTAINS) };
		}
		else {
			linkConditionNames = new TranslatedObject[] { 
                    TextUtils.createTranslatedString(ConditionFactory.FILTER_EXIST), 
                    TextUtils.createTranslatedString(ConnectorStyleCondition.FILTER_STYLE) 
			        };
		}
		return new DefaultComboBoxModel(linkConditionNames);
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_LINK));
		list.addElement(TextUtils.createTranslatedString(CONNECTOR_LABEL));
		list.addElement(TextUtils.createTranslatedString(CONNECTOR));
		return list;
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		return new FixedBasicComboBoxEditor();
	}

	public ComboBoxModel getValuesForProperty(final Object property, TranslatedObject simpleCond) {
	    if(simpleCond.objectEquals(ConnectorStyleCondition.FILTER_STYLE)) {
	        final MapModel map = Controller.getCurrentController().getMap();
	        MapStyleModel styleMap = MapStyleModel.getExtension(map);
	        IStyle[] styles = styleMap.getStyles().stream().filter(key -> 
	         NodeLinks.getSelfConnector(styleMap.getStyleNode(key)).isPresent())
	        .toArray(IStyle[]::new);
	        return new DefaultComboBoxModel<>(styles);
	    }
		return values;
	}

	public boolean isCaseDependent(final Object property, final TranslatedObject simpleCond) {
		return ((TranslatedObject) property).objectEquals(CONNECTOR_LABEL) ||
			   ((TranslatedObject) property).objectEquals(FILTER_LINK);
	}
	
	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return ((TranslatedObject) property).objectEquals(CONNECTOR_LABEL) ||
			   ((TranslatedObject) property).objectEquals(FILTER_LINK);
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(HyperLinkEqualsCondition.NAME)) {
			final String target = element.getAttribute(HyperLinkEqualsCondition.TEXT, null);
			final boolean matchCase = Boolean.toString(true).equals(
				    element.getAttribute(StringConditionAdapter.MATCH_CASE, null));
			final boolean matchApproximately = Boolean.toString(true).equals(
					element.getAttribute(StringConditionAdapter.MATCH_APPROXIMATELY, null));
			return new HyperLinkEqualsCondition(target, matchCase, matchApproximately,
			        Boolean.valueOf(element.getAttribute(StringConditionAdapter.IGNORE_DIACRITICS, null)));
		}
		if (element.getName().equalsIgnoreCase(HyperLinkContainsCondition.NAME)) {
			final String target = element.getAttribute(HyperLinkContainsCondition.TEXT, null);
			final boolean matchCase = Boolean.toString(true).equals(
				    element.getAttribute(HyperLinkContainsCondition.MATCH_CASE, null));
			final boolean matchApproximately = Boolean.toString(true).equals(
					element.getAttribute(HyperLinkContainsCondition.MATCH_APPROXIMATELY, null));
			return new HyperLinkContainsCondition(target, matchCase, matchApproximately,
			        Boolean.valueOf(element.getAttribute(StringConditionAdapter.IGNORE_DIACRITICS, null)));
		}
		if (element.getName().equalsIgnoreCase(HyperLinkExistsCondition.NAME)) {
			return new HyperLinkExistsCondition();
		}
		if (element.getName().equalsIgnoreCase(ConnectorLabelEqualsCondition.NAME)) {
			final String text = element.getAttribute(ConnectorLabelEqualsCondition.TEXT, null);
			final boolean matchCase = Boolean.toString(true).equals(
			    element.getAttribute(ConnectorLabelEqualsCondition.MATCH_CASE, null));
			final boolean matchApproximately = Boolean.toString(true).equals(
				    element.getAttribute(ConnectorLabelEqualsCondition.MATCH_APPROXIMATELY, null));
			return new ConnectorLabelEqualsCondition(text, matchCase, matchApproximately,
			        Boolean.valueOf(element.getAttribute(StringConditionAdapter.IGNORE_DIACRITICS, null)));
		}
		if (element.getName().equalsIgnoreCase(ConnectorLabelContainsCondition.NAME)) {
			final String text = element.getAttribute(ConnectorLabelContainsCondition.TEXT, null);
			final boolean matchCase = Boolean.toString(true).equals(
			    element.getAttribute(ConnectorLabelEqualsCondition.MATCH_CASE, null));
			final boolean matchApproximately = Boolean.toString(true).equals(
				    element.getAttribute(ConnectorLabelEqualsCondition.MATCH_APPROXIMATELY, null));
			return new ConnectorLabelContainsCondition(text, matchCase, matchApproximately,
			        Boolean.valueOf(element.getAttribute(StringConditionAdapter.IGNORE_DIACRITICS, null)));
		}
        if (element.getName().equalsIgnoreCase(ConnectorExistsCondition.NAME)) {
            return new ConnectorExistsCondition();
        }
        if (element.getName().equalsIgnoreCase(ConnectorStyleCondition.NAME)) {
            return ConnectorStyleCondition.load(element);
        }
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
		if (((TranslatedObject)selectedProperty).objectEquals(CONNECTOR)
		        ||
			(((TranslatedObject)selectedProperty).objectEquals(FILTER_LINK) &&
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
}
