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

import java.util.List;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class TagContainsCondition extends StringConditionAdapter {
	private static final String NAME = "tag_contains_condition";
    static final String VALUE = "VALUE";
    private static final String SEARCH_IN_CATEGORIES = "SEARCH_IN_CATEGORIES";

	static ASelectableCondition load(final XMLElement element) {
		return new TagContainsCondition(
            element.getAttribute(VALUE, null),
            Boolean.valueOf(element.getAttribute(MATCH_CASE, null)),
            Boolean.valueOf(element.getAttribute(MATCH_APPROXIMATELY, null)),
            Boolean.valueOf(element.getAttribute(MATCH_WORDWISE, null)),
            Boolean.valueOf(element.getAttribute(IGNORE_DIACRITICS, null)),
            Boolean.valueOf(element.getAttribute(SEARCH_IN_CATEGORIES, null))
		    );
	}

	final private String value;
	final private boolean searchesInCategories;
	final private String comparedValue;
    final private StringMatchingStrategy stringMatchingStrategy;

    /**
	 */
	public TagContainsCondition(final String value, final boolean matchCase,
			final boolean matchApproximately,
			final boolean matchWordwise, boolean ignoreDiacritics,
			boolean searchesInCategories) {
		super(matchCase, matchApproximately, matchWordwise, ignoreDiacritics);
        this.value = value;
        this.comparedValue = value;
        this.searchesInCategories = searchesInCategories;
        this.stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
            StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
	 * .MindMapNode)
	 */
	@Override
    public boolean checkNode(final NodeModel node) {
	    final IconController iconController = IconController.getController();
	    final List<Tag> tags = iconController.getTags(node);
	    if(searchesInCategories) {
	        final List<CategorizedTag> categorizedTags = iconController.categorizedTags(tags, node.getMap().getIconRegistry());
	        for (CategorizedTag tag : categorizedTags) {
	            if (checkText(tag.getContent()))
	                return true;
	        }
	    }
	    for (Tag tag : tags) {
	        if (checkText(tag.getContent()))
	            return true;

	    }
	    return false;
	}

	private boolean checkText(String text) {
	    return stringMatchingStrategy.matches(normalizedValue(), normalize(text), substringMatchType());
	}

	@Override
	protected String createDescription() {
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
		return createDescription(TextUtils.getText(IconConditionController.FILTER_TAG), simpleCondition, value);
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		if(searchesInCategories)
		    child.setAttribute(SEARCH_IN_CATEGORIES, "true");
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
