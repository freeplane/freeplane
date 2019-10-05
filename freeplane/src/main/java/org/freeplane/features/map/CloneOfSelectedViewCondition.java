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
package org.freeplane.features.map;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionSnapshotFactory;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;

public class CloneOfSelectedViewCondition extends ASelectableCondition implements ConditionSnapshotFactory{
	static final String NAME = "clone_condition";
	private static String description;

	public static ASelectableCondition CreateCondition() {
		return new CloneOfSelectedViewCondition();
	}

	public CloneOfSelectedViewCondition() {
		super();
	}

	public boolean checkNode(final NodeModel node) {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		if (selection != null){
			for(NodeModel clone : node.allClones())
				if (selection.isSelected(clone))
					return true;
		}
		return false;
	}

	@Override
    protected String createDescription() {
		if (CloneOfSelectedViewCondition.description == null) {
			CloneOfSelectedViewCondition.description = TextUtils.getText("filter_clones");
		}
		return CloneOfSelectedViewCondition.description;
    }

	@Override
    protected String getName() {
	    return NAME;
    }

	public static ASelectableCondition load(XMLElement element) {
	    return new CloneOfSelectedViewCondition();
    }

	public ASelectableCondition createSnapshotCondition() {
	    return new CloneOfSelectedViewSnapshotCondition(Controller.getCurrentController().getSelection().getSelection());
    }
}
