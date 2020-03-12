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

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.condition.ConditionFactory;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public class AliasEqualsCondition extends AliasCondition {
	public static final String NAME = "alias_equals";

	private final StringMatchingStrategy stringMatchingStrategy;

	public AliasEqualsCondition(final String alias, final boolean matchCase, final boolean matchApproximately, boolean ignoreDiacritics) {
		super(alias, matchCase, matchApproximately, ignoreDiacritics);
		this.stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
			StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
	}


    @Override
	protected boolean checkAlias(final String alias) {
		return stringMatchingStrategy.matches(normalizedValue(), normalize(alias), false);
	}


	@Override
	protected String createDescription() {
		final String condition = TextUtils.getText(MapExplorerConditionController.FILTER_ALIAS);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_IS_EQUAL_TO);
		return createDescription(condition, simpleCondition, getAlias());
	}

	@Override
	protected String getName() {
		return NAME;
	}

}
