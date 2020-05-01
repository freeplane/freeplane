/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.map;

import java.util.Collection;
import java.util.HashSet;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;

/**
 * @author Dimitry Polivaev
 * 26.04.2014
 */
public class CloneOfSelectedViewSnapshotCondition extends ASelectableCondition {
	private static final String NAME = "clone_snapshot";
	private static String description;

	HashSet<NodeModel> selectedNodes;

	public CloneOfSelectedViewSnapshotCondition(Collection<NodeModel> selectedNodes) {
		super();
		this.selectedNodes = new HashSet<NodeModel>();
		this.selectedNodes.addAll(selectedNodes);
	}

	public boolean checkNode(final NodeModel node) {
		for(NodeModel clone : node.allClones())
			if (selectedNodes.contains(clone))
				return true;
		return false;
	}

	@Override
    protected String createDescription() {
		if (CloneOfSelectedViewSnapshotCondition.description == null) {
			CloneOfSelectedViewSnapshotCondition.description = TextUtils.getText("filter_clone_snapshot");
		}
		return CloneOfSelectedViewSnapshotCondition.description;
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
