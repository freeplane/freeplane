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

import java.awt.FontMetrics;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.StringMatchingStrategy.Type;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.ConditionFactory.ConditionOperator;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
class AliasStartsWithCondition extends AliasCondition {
	static final String NAME = "alias_starts_with";

	private final StringMatchingStrategy stringMatchingStrategy;

	AliasStartsWithCondition(final String alias, final boolean matchCase, final boolean matchApproximately, boolean ignoreDiacritics) {
		super(alias, matchCase, matchApproximately, ignoreDiacritics);
		this.stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
			StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
	}

	@Override
	protected boolean checkAlias(final String alias) {
		String searchedAlias = getAlias();
		int searchedLength = searchedAlias.length();
		return alias.length() >= searchedLength
				&& stringMatchingStrategy.matches(normalizedValue(), normalize(alias.substring(0, searchedLength)), Type.ALL);
	}


	@Override
	protected String createDescription() {
		final String condition = TextUtils.getText(MapExplorerConditionController.FILTER_ALIAS);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_STARTS_WITH);
		return createDescription(condition, simpleCondition, getAlias());
	}

    @Override
    protected List<Icon> createRenderedIcons(FontMetrics fontMetrics) {
        final String condition = TextUtils.getText(MapExplorerConditionController.FILTER_ALIAS);
        return createRenderedIcons(condition, ConditionOperator.FILTER_STARTS_WITH, getAlias(), fontMetrics);
    }

	@Override
	protected String getName() {
		return NAME;
	}

}
