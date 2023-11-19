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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

@SuppressWarnings("serial")
class ShowDependingNodesAction extends AFreeplaneAction {
    MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();

    private DependencyDirection dependencyDirection;
    private CodeNodeSelection codeNodeSelection;

    enum DependencyDirection {
        INCOMING(CodeNodeModel::getIncomingDependenciesWithKnownTargets),
        OUTGOING(CodeNodeModel::getOutgoingDependenciesWithKnownTargets),
        INCOMING_AND_OUTGOING(CodeNodeModel::getIncomingAndOutgoingDependenciesWithKnownTargets);

        final Function<CodeNodeModel, Stream<Dependency>> nodeDependencies;

        private DependencyDirection(Function< CodeNodeModel, Stream<Dependency>> nodeDependencies) {
            this.nodeDependencies = nodeDependencies;
        }
    }

    private static Stream<JavaClass> dependentClasses(Dependency dependency) {
        JavaClass origin = CodeNodeModel.findEnclosingNamedClass(dependency.getOriginClass());
        JavaClass target = CodeNodeModel.findEnclosingNamedClass(dependency.getTargetClass());
        return origin != target ? Stream.of(origin, target) : Stream.empty();
    }


    ShowDependingNodesAction(DependencyDirection dependencyDirection,
            CodeNodeSelection codeNodeSelection) {
	    super("code.ShowDependingNodesAction." + dependencyDirection + "." + codeNodeSelection,
	            TextUtils.format("code.ShowDependingNodesAction.text",
	                    TextUtils.getRawText("code." + dependencyDirection),
	                    TextUtils.getRawText("code." + codeNodeSelection)),
	            null);
        this.dependencyDirection = dependencyDirection;
        this.codeNodeSelection = codeNodeSelection;
    }

	@Override
    public void actionPerformed(ActionEvent e) {
	    IMapSelection selection = Controller.getCurrentController().getSelection();
	    if(selection.getFilter().getCondition() == null)
	        return;
        Set<String> dependentNodeIDs = codeNodeSelection.get()
	            .flatMap(dependencyDirection.nodeDependencies)
	            .flatMap(ShowDependingNodesAction::dependentClasses)
	            .map(JavaClass::getName)
	            .collect(Collectors.toSet());
        ASelectableCondition condition = new DependencySnapshotCondition(dependentNodeIDs);
	    Filter filter = new Filter(condition, false, true, false, false, null);
	    FilterController filterController = FilterController.getCurrentFilterController();
        filterController.applyFilter(selection.getMap(), false, filter);
	}

}
