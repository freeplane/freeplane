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
package org.freeplane.features.filter.condition;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class SelectedViewCondition extends ASelectableCondition implements ConditionSnapshotFactory{
	private static final String NAME = "selected_view_condition";
	private static String description;

	public static ASelectableCondition CreateCondition() {
		return new SelectedViewCondition();
	}

// // 	private final Controller controller;

	public SelectedViewCondition() {
		super();
//		this.controller = controller;
	}

	public boolean checkNode(final NodeModel node) {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		return selection != null && selection.isSelected(node);
	}

	@Override
    protected String createDescription() {
		if (SelectedViewCondition.description == null) {
			SelectedViewCondition.description = TextUtils.getText("filter_selected_node_view");
		}
		return SelectedViewCondition.description;
    }

	@Override
    protected String getName() {
	    return NAME;
    }
	
    @Override
    public boolean canBePersisted() {
        return false;
    }
 
	@Override
    public ASelectableCondition createSnapshotCondition() {
	    return  new SelectedViewSnapshotCondition(Controller.getCurrentController().getSelection().getSelection());
    }
}
