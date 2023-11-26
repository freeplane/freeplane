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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

@SuppressWarnings("serial")
class ShowDependingNodesAction extends AFreeplaneAction {
    enum Depth{NO_RECURSION(1), MAXIMUM_RECURSION(10000);

    final int depth;

    Depth(int depth) {
        this.depth = depth;
        // TODO Auto-generated constructor stub
    }}
    MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();

    private DependencyDirection dependencyDirection;
    private CodeNodeSelection codeNodeSelection;

    private final int maximumRecursionDepth;

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
            CodeNodeSelection codeNodeSelection, Depth recursionDepth) {
	    super("code.ShowDependingNodesAction." + dependencyDirection + "." + codeNodeSelection + "." + recursionDepth,
	            formatActionText(dependencyDirection, codeNodeSelection, recursionDepth),
	            null);
        this.dependencyDirection = dependencyDirection;
        this.codeNodeSelection = codeNodeSelection;
        this.maximumRecursionDepth = recursionDepth.depth;
    }


    static String formatActionText(DependencyDirection dependencyDirection,
            CodeNodeSelection codeNodeSelection, Depth recursionDepth) {
        return TextUtils.format("code.ShowDependingNodesAction." + recursionDepth + ".text",
                TextUtils.getRawText("code." + dependencyDirection),
                TextUtils.getRawText("code." + codeNodeSelection));
    }

	@Override
    public void actionPerformed(ActionEvent e) {
	    IMapSelection selection = Controller.getCurrentController().getSelection();
	    ICondition currentCondition = selection.getFilter().getCondition();
	    if(currentCondition == null)
	        return;
	    MapModel map = selection.getMap();
	    ((CodeNodeModel)map.getRootNode()).loadSubtree();
	    Set<String> dependentNodeIDs = dependencies(codeNodeSelection.get());
	    for(int recursionCounter = allNodesSatisfyFilter(selection, dependentNodeIDs) ? 0 : 1;
	        recursionCounter < maximumRecursionDepth;
	        recursionCounter++) {
	        Set<String> next = dependencies(dependentNodeIDs.stream().map(map::getNodeForID).map(CodeNodeModel.class::cast));
	        next.removeAll(dependentNodeIDs);
	        if(next.isEmpty())
	            break;
	        dependentNodeIDs.addAll(next);
	        if(allNodesSatisfyFilter(selection, next))
	            recursionCounter--;
	    }
	    dependentNodeIDs.removeIf(id -> currentCondition.checkNode(map.getNodeForID(id)));
        FilterController filterController = FilterController.getCurrentFilterController();
        ASelectableCondition condition = new DependencySnapshotCondition(dependentNodeIDs,
                currentCondition);
	    Filter filter = new Filter(condition, false, true, false, false, null);
        filterController.applyFilter(map, false, filter);
	}


    private boolean allNodesSatisfyFilter(IMapSelection selection, Set<String> dependentNodeIDs) {
        return dependentNodeIDs.stream().allMatch(id -> selection.getMap().getNodeForID(id).isVisible(selection.getFilter()));
    }


    HashSet<String> dependencies(Stream<CodeNodeModel> startingNodes) {
        return startingNodes
	            .flatMap(dependencyDirection.nodeDependencies)
	            .flatMap(ShowDependingNodesAction::dependentClasses)
	            .map(JavaClass::getName)
	            .collect(Collectors.toCollection(HashSet::new));
    }

}
