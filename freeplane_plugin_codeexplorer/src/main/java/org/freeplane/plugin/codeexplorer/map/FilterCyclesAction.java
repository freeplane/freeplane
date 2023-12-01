/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
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
package org.freeplane.plugin.codeexplorer.map;

import java.awt.event.ActionEvent;
import java.util.Set;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.SelectedViewSnapshotCondition;
import org.freeplane.features.mode.Controller;

@SuppressWarnings("serial")
class FilterCyclesAction extends AFreeplaneAction {

    public FilterCyclesAction() {
	    super("code.FilterCyclesAction");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CodeNode node = (CodeNode) Controller.getCurrentController().getSelection().getSelected();

        Set<CodeNode> cycleNodes = node.findCyclicDependencies();
        if(! cycleNodes.isEmpty()) {
            ASelectableCondition condition = new SelectedViewSnapshotCondition(cycleNodes);
            Filter filter = new Filter(condition, false, true, false, false, null);
            FilterController filterController = FilterController.getCurrentFilterController();
            filterController.applyFilter(node.getMap(), false, filter);
        }
    }
}
