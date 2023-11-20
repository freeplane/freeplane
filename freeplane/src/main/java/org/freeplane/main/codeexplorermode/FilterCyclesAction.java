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
package org.freeplane.main.codeexplorermode;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.core.domain.JavaClass;

@SuppressWarnings("serial")
class FilterCyclesAction extends AFreeplaneAction {
	private final CodeNodeSelection selection;

    public FilterCyclesAction(CodeNodeSelection selection) {
	    super("code.FILTER" + "." + selection + ".CyclesAction");
        this.selection = selection;
    }

	@Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = Controller.getCurrentController();
        MapView mapView = (MapView) controller.getMapViewManager().getMapViewComponent();
        GraphCycleFinder<String> cycleFinder = new GraphCycleFinder<>();
        if(selection == CodeNodeSelection.SELECTED) {
            selection.get()
            .flatMap(node -> node.getClassesInPackageTree().stream())
            .map(CodeNodeModel::findEnclosingNamedClass)
            .map(JavaClass::getName)
            .forEach(cycleFinder::addNode);
            cycleFinder.stopSearchHere();
        }

        CodeNodeStream.visibleNodes(mapView)
        .map(CodeNodeModel.class::cast)
        .flatMap(CodeNodeModel::getOutgoingDependenciesWithKnownTargets)
        .forEach(dep -> cycleFinder.addEdge(
                CodeNodeModel.findEnclosingNamedClass(dep.getOriginClass()).getName(),
                CodeNodeModel.findEnclosingNamedClass(dep.getTargetClass()).getName()));

        Set<String> cycleNodes = cycleFinder.findSimpleCycles().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        if(! cycleNodes.isEmpty()) {
            ASelectableCondition condition = new DependencySnapshotCondition(cycleNodes);
            Filter filter = new Filter(condition, false, true, false, false, null);
            FilterController filterController = FilterController.getCurrentFilterController();
            filterController.applyFilter(mapView.getMap(), false, filter);
        }
	}
}
