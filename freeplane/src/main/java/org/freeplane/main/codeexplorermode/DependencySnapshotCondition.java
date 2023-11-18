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
package org.freeplane.main.codeexplorermode;

import java.util.Set;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;

class DependencySnapshotCondition extends ASelectableCondition {
	private static final String NAME = "dependencies_snapshot";
	private static String description;

	Set<String> dependentNodeIDs;

	public DependencySnapshotCondition(Set<String> dependentNodeIDs) {
		super();
		this.dependentNodeIDs = dependentNodeIDs;
	}

	@Override
    public boolean checkNode(final NodeModel node) {
		return dependentNodeIDs.contains(node.getID());
	}

	@Override
    protected String createDescription() {
		if (DependencySnapshotCondition.description == null) {
			DependencySnapshotCondition.description = TextUtils.getText("filter_dependencies_snapshot");
		}
		return DependencySnapshotCondition.description;
    }

	@Override
    protected String getName() {
	    return NAME;
   }

    @Override
    public boolean canBePersisted() {
        return false;
    }

}
