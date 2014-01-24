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

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.ExactStringMatchingStrategy;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class AttributeContainsCondition extends ASelectableCondition {
	static final String ATTRIBUTE = "ATTRIBUTE";
	static final String NAME = "attribute_contains_condition";
    static final String VALUE = "VALUE";
    static final String MATCH_CASE = "MATCH_CASE";
	static final String MATCH_APPROXIMATELY = "MATCH_APPROXIMATELY";
    	

	static ASelectableCondition load(final XMLElement element) {
		return new AttributeContainsCondition(
            element.getAttribute(AttributeContainsCondition.ATTRIBUTE, null),
            element.getAttribute(AttributeContainsCondition.VALUE, null),
            Boolean.valueOf(element.getAttribute(AttributeContainsCondition.MATCH_CASE, null)),
            Boolean.valueOf(element.getAttribute(AttributeContainsCondition.MATCH_APPROXIMATELY, null))
		    );
	}

	final private String attribute;
	final private String value;
	final private String comparedValue;
	final private boolean matchCase;
	final private boolean matchApproximately;
    final private StringMatchingStrategy stringMatchingStrategy;

    /**
	 */
	public AttributeContainsCondition(final String attribute,final String value, final boolean matchCase,
			final boolean matchApproximately) {
		super();
        this.attribute = attribute;
        this.value = value;
        this.matchCase = matchCase;
        //this.comparedValue = matchCase ? value : value.toLowerCase();
        this.comparedValue = value;
        this.matchApproximately = matchApproximately;
        this.stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
        	new ExactStringMatchingStrategy();
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
            if (stringMatchingStrategy.matches(comparedValue, text, true, matchCase)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String createDescription() {
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
		return ConditionFactory.createDescription(attribute, simpleCondition, value, matchCase, matchApproximately);
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(AttributeContainsCondition.ATTRIBUTE, attribute);
        child.setAttribute(AttributeContainsCondition.VALUE, value);
        child.setAttribute(AttributeContainsCondition.MATCH_CASE, Boolean.toString(matchCase));
        child.setAttribute(AttributeContainsCondition.MATCH_APPROXIMATELY, Boolean.toString(matchApproximately));
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
