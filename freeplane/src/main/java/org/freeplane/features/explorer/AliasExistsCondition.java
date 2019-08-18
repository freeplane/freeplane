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
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public class AliasExistsCondition extends ASelectableCondition {
	public static final String NAME = "alias_exists";

	public AliasExistsCondition() {
		super();
	}

	@Override
	protected String createDescription() {
		final String condition = TextUtils.getText(MapExplorerConditionController.FILTER_ALIAS);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_EXIST);
		return ConditionFactory.createDescription(condition, simpleCondition, null, false, false);
	}

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	public boolean checkNode(NodeModel node) {
		return !NodeAlias.getAlias(node).isEmpty();
	}
	
}
