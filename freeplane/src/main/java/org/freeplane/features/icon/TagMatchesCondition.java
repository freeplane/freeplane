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

import java.util.regex.Pattern;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * Condition for matching a regexp against an attribute.
 * Approximate matching setting is ignored here.
 *
 * @author Dimitry Polivaev
 */
public class TagMatchesCondition extends TagCondition {
	static final String NAME = "tag_matches_condition";
    static final String VALUE = "VALUE";

	static ASelectableCondition load(final XMLElement element) {
		return new TagMatchesCondition(
            element.getAttribute(VALUE, null),
            Boolean.valueOf(element.getAttribute(StringConditionAdapter.MATCH_CASE, null)),
            Boolean.valueOf(element.getAttribute(SEARCH_IN_CATEGORIES, null))
		    );
	}

	final private String value;
	final private Pattern searchPattern;
	/**
	 */
	public TagMatchesCondition(final String value, final boolean matchCase, boolean searchesInCategories) {
		super(value, matchCase, false, false, false, searchesInCategories);
        this.value = value;
        int flags = Pattern.DOTALL;
        if (!matchCase) {
			flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }
        this.searchPattern = Pattern.compile(value, flags);
	}

    @Override
    protected boolean checkTag(CategorizedTag categorizedTag) {
        return categorizedTag.categoryTags().stream().anyMatch(tag -> checkText(tag.getContent()));
    }

	@Override
    protected boolean checkText(String text) {
	    return searchPattern.matcher(text).find();
    }

	@Override
	protected String createDescription() {
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_REGEXP);
		return ConditionFactory.createDescription(TextUtils.getText(IconConditionController.FILTER_TAG),
		        simpleCondition, value, isMatchCase(), false, false);
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
    private boolean isMatchCase() {
        return (searchPattern.flags() & Pattern.CASE_INSENSITIVE) == 0;
    }

}
