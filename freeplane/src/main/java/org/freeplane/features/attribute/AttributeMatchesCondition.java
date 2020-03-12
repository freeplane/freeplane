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
import org.freeplane.features.filter.condition.StringConditionAdapter;
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

	static ASelectableCondition load(final XMLElement element) {
		return new AttributeMatchesCondition(
			AttributeConditionController.toAttributeObject(element.getAttribute(ATTRIBUTE, null)),
            element.getAttribute(VALUE, null),
            Boolean.valueOf(element.getAttribute(StringConditionAdapter.MATCH_CASE, null))
		    );
	}

	final private Object attribute;
	final private String value;
	final private Pattern searchPattern;
	/**
	 */
	public AttributeMatchesCondition(final Object attribute,final String value, final boolean matchCase) {
		super();
        this.attribute = attribute;
        this.value = value;
        int flags = Pattern.DOTALL;
        if (!matchCase) {
			flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
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
			if(attribute.equals(AttributeConditionController.ANY_ATTRIBUTE_NAME_OR_VALUE_OBJECT)){
				if (checkText(attributes.getValueAt(i, 0).toString()))
					return true;
				
			}
			else if(! attributes.getValueAt(i, 0).equals(attribute)) {
                continue;
            }
            final Object originalContent = attributes.getValueAt(i, 1);
            String text = textController.getTransformedTextNoThrow(originalContent, node, null);
            if(checkText(text))
                return true;
		}
		return false;
	}

	private boolean checkText(String text) {
	    return searchPattern.matcher(text).find();
    }

	@Override
	protected String createDescription() {
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_REGEXP);
		return ConditionFactory.createDescription(attribute.toString(), simpleCondition, value, isMatchCase(), false, false);
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		if (attribute instanceof String) child.setAttribute(ATTRIBUTE, (String) attribute);
        child.setAttribute(VALUE, value);
        child.setAttribute(StringConditionAdapter.MATCH_CASE, Boolean.toString(isMatchCase()));
	}

	@Override
    protected String getName() {
	    return NAME;
    }
    private boolean isMatchCase() {
        return (searchPattern.flags() & Pattern.CASE_INSENSITIVE) == 0;
    }

}
