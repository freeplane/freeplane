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
package org.freeplane.features.icon;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.IAttributeTableModel;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class TagContainsCondition extends StringConditionAdapter {
	static final String NAME = "tag_contains_condition";
    static final String VALUE = "VALUE";

	static ASelectableCondition load(final XMLElement element) {
		return new TagContainsCondition(
            element.getAttribute(VALUE, null),
            Boolean.valueOf(element.getAttribute(MATCH_CASE, null)),
            Boolean.valueOf(element.getAttribute(MATCH_APPROXIMATELY, null)),
            Boolean.valueOf(element.getAttribute(IGNORE_DIACRITICS, null))
		    );
	}

	final private String value;
	final private String comparedValue;
    final private StringMatchingStrategy stringMatchingStrategy;

    /**
	 */
	public TagContainsCondition(final String value, final boolean matchCase,
			final boolean matchApproximately, boolean ignoreDiacritics) {
		super(matchCase, matchApproximately, ignoreDiacritics);
        this.value = value;
        this.comparedValue = value;
        this.stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
        	StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
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
		 final IconController iconController = IconController.getController();
		 for (Tag tag : iconController.getTags(node)) {
			    if (checkText(tag.getContent()))
					return true;

		}
		return false;
	}

	private boolean checkText(String text) {
	    return stringMatchingStrategy.matches(normalizedValue(), normalize(text), true);
    }

	@Override
	protected String createDescription() {
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
		return createDescription(TextUtils.getText(IconConditionController.FILTER_TAG), simpleCondition, value);
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
        child.setAttribute(VALUE, value);
	}

	@Override
    protected String getName() {
	    return NAME;
    }

    @Override
    protected String conditionValue() {
        return comparedValue;
    }
}
