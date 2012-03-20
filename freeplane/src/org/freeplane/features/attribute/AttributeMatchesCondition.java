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
package org.freeplane.features.attribute;

import java.util.regex.Pattern;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * Condition for matching a regexp against an attribute.
 * Approximate matching setting is ignored here.
 * 
 * @author Dimitry Polivaev
 */
public class AttributeMatchesCondition extends ASelectableCondition {
	static final String ATTRIBUTE = "ATTRIBUTE";
	static final String NAME = "attribute_matches_condition";
    static final String VALUE = "VALUE";
    static final String MATCH_CASE = "MATCH_CASE";

	static ASelectableCondition load(final XMLElement element) {
		return new AttributeMatchesCondition(
            element.getAttribute(AttributeMatchesCondition.ATTRIBUTE, null),
            element.getAttribute(AttributeMatchesCondition.VALUE, null),
            Boolean.valueOf(element.getAttribute(AttributeMatchesCondition.MATCH_CASE, null))
		    );
	}

	final private String attribute;
	final private String value;
	final private Pattern searchPattern;
	/**
	 */
	public AttributeMatchesCondition(final String attribute,final String value, final boolean matchCase) {
		super();
        this.attribute = attribute;
        this.value = value;
        int flags = Pattern.DOTALL;
        if (!matchCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        this.searchPattern = Pattern.compile(value, flags);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
	 * .MindMapNode)
	 */
	public boolean checkNode(final NodeModel node) {
		final IAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		final TextController textController = TextController.getController();
		for (int i = 0; i < attributes.getRowCount(); i++) {
            if(! attributes.getValueAt(i, 0).equals(attribute)) {
                continue;
            }
            final Object originalContent = attributes.getValueAt(i, 1);
            String text = textController.getTransformedTextNoThrow(originalContent, node, null);
            if(searchPattern.matcher(text).find())
                return true;
		}
		return false;
	}

	@Override
	protected String createDescription() {
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_REGEXP);
		return ConditionFactory.createDescription(attribute, simpleCondition, null, isMatchCase(), false);
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(AttributeMatchesCondition.ATTRIBUTE, attribute);
        child.setAttribute(AttributeMatchesCondition.VALUE, value);
        child.setAttribute(AttributeMatchesCondition.MATCH_CASE, Boolean.toString(isMatchCase()));
	}

	@Override
    protected String getName() {
	    return NAME;
    }
    private boolean isMatchCase() {
        return (searchPattern.flags() & Pattern.CASE_INSENSITIVE) == 0;
    }

}
