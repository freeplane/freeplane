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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeStream;
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
    }}
    MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();

    private DependencyDirection dependencyDirection;
    private CodeNodeSelection codeNodeSelection;

    private final int maximumRecursionDepth;

    enum DependencyDirection {
        INHERITED(CodeNode::getInherited), INHERITING(CodeNode::getInheriting),
        INCOMING(dependentClassesFunction(CodeNode::getIncomingDependenciesWithKnownOrigins)),
        OUTGOING(dependentClassesFunction(CodeNode::getOutgoingDependenciesWithKnownTargets)),
        INCOMING_AND_OUTGOING(dependentClassesFunction(CodeNode::getIncomingAndOutgoingDependenciesWithKnownTargets)),
        CONNECTED(dependentClassesFunction(CodeNode::getIncomingAndOutgoingDependenciesWithKnownTargets));

        private static Stream<JavaClass> dependentClasses(Dependency dependency) {
            JavaClass origin = CodeNode.findEnclosingNamedClass(dependency.getOriginClass());
            JavaClass target = CodeNode.findEnclosingNamedClass(dependency.getTargetClass());
            return origin != target ? Stream.of(origin, target) : Stream.empty();
        }

        private static Function<CodeNode, Stream<JavaClass>> dependentClassesFunction(Function<CodeNode, Stream<Dependency>> nodeDependencies) {
            return nodeDependencies.andThen(dependencies -> dependencies.flatMap(DependencyDirection::dependentClasses));
        }

        final Function<CodeNode, Stream<JavaClass>> nodeDependencies;

        private DependencyDirection(Function<CodeNode, Stream<JavaClass>> relatedClasses) {
            this.nodeDependencies = relatedClasses;
        }

    }


    ShowDependingNodesAction(DependencyDirection dependencyDirection,
            CodeNodeSelection codeNodeSelection, Depth recursionDepth) {
	    super("code.ShowDependingNodesAction." + dependencyDirection + "." + codeNodeSelection + "." + recursionDepth,
	            formatActionText(dependencyDirection, codeNodeSelection, recursionDepth),
	            null);
	    setIcon(getIconKey());
        this.dependencyDirection = dependencyDirection;
        this.codeNodeSelection = codeNodeSelection;
        this.maximumRecursionDepth = recursionDepth.depth;
    }


    private static String formatActionText(DependencyDirection dependencyDirection,
            CodeNodeSelection codeNodeSelection, Depth recursionDepth) {
        if(dependencyDirection.ordinal() <= DependencyDirection.INHERITING.ordinal())
            return TextUtils.format("code.ShowDependingNodesAction." + dependencyDirection + "." + recursionDepth + ".text",
                    TextUtils.getRawText("code." + codeNodeSelection));
        else
            return TextUtils.format("code.ShowDependingNodesAction." + recursionDepth + ".text",
                TextUtils.getRawText("code." + dependencyDirection),
                TextUtils.getRawText("code." + codeNodeSelection));
    }

	@Override
    public void actionPerformed(ActionEvent e) {
	    final Controller controller = Controller.getCurrentController();
        IMapSelection selection = controller.getSelection();
	    Filter lastFilter = selection.getFilter();
        ICondition currentCondition = lastFilter.getCondition();
	    if(currentCondition == null) {
	        controller.getViewController().out(TextUtils.format("code.n_hidden_dependencies_revealed", 0));
	        UITools.informationMessage(TextUtils.getRawText("code.no_hidden_dependencies_found"));
            return;
        }
	    MapModel map = selection.getMap();
	    Set<String> dependentNodeIDs;
        if (dependencyDirection == DependencyDirection.INCOMING_AND_OUTGOING) {
            Set<String> incomingDependencies = recursiveDependencies(selection, currentCondition, map, DependencyDirection.INCOMING);
            Set<String> outgoingDependencies = recursiveDependencies(selection, currentCondition, map, DependencyDirection.OUTGOING);
            if(incomingDependencies.isEmpty()) {
                dependentNodeIDs = outgoingDependencies;
            } else {
                dependentNodeIDs = incomingDependencies;
                dependentNodeIDs.addAll(outgoingDependencies);
            }
        } else
            dependentNodeIDs = recursiveDependencies(selection, currentCondition, map, dependencyDirection);
        if(! dependentNodeIDs.isEmpty()) {
            codeNodeSelection.get()
            .filter(node -> ! currentCondition.checkNode(node))
            .map(NodeModel::getID)
            .forEach(dependentNodeIDs::add);
            FilterController filterController = FilterController.getCurrentFilterController();
            ASelectableCondition condition = new DependencySnapshotCondition(dependentNodeIDs,
                    currentCondition, ActionUtils.getActionTitle(this));
            Filter filter = new Filter(condition, false, true, lastFilter.areDescendantsShown(), false,
                    lastFilter.getFilteredElement(), null);
            filterController.applyFilter(map, false, filter);
            if(! lastFilter.areAncestorsShown()) {
                AncestorsHider.hideAncestors();
            }
            controller.getViewController().out(TextUtils.format("code.n_hidden_dependencies_revealed", dependentNodeIDs.size()));
        } else {
            controller.getViewController().out(TextUtils.format("code.n_hidden_dependencies_revealed", 0));
            UITools.informationMessage(TextUtils.getRawText("code.no_hidden_dependencies_found"));
        }
	}


    private Set<String> recursiveDependencies(IMapSelection selection, ICondition currentCondition,
            MapModel map, DependencyDirection dependencyDirection) {
        DependencySelection dependencySelection = new DependencySelection(selection);
	    final Filter filter = selection.getFilter();
        Set<String> dependentNodeIDs = dependencies(codeNodeSelection.get(), dependencySelection.getMap(), filter, dependencyDirection);
	    for(int recursionCounter = allNodesSatisfyFilter(selection, dependentNodeIDs) ? 0 : 1;
	        recursionCounter < maximumRecursionDepth;
	        recursionCounter++) {
	        Set<String> next = dependencies(dependentNodeIDs.stream()
	                .map(map::getNodeForID)
	                .map(CodeNode.class::cast), dependencySelection.getMap(), null, dependencyDirection);
	        next.removeAll(dependentNodeIDs);
	        if(next.isEmpty()) {
	            if(recursionCounter == 0)
	                return Collections.emptySet();
	            else
	                break;
            }
	        dependentNodeIDs.addAll(next);
	        if(allNodesSatisfyFilter(selection, next))
	            recursionCounter--;
	        else
	            continue;
	    }
	    dependentNodeIDs.removeIf(id -> currentCondition.checkNode(map.getNodeForID(id)));
        return dependentNodeIDs;
    }


    private boolean allNodesSatisfyFilter(IMapSelection selection, Set<String> dependentNodeIDs) {
        Filter filter = selection.getFilter();
        return filter == null || dependentNodeIDs.stream()
                .allMatch(id -> filter.accepts(selection.getMap().getNodeForID(id)));
    }


    private static HashSet<String> dependencies(Stream<CodeNode> startingNodes, CodeMap map, Filter filter, DependencyDirection dependencyDirection) {
        Stream<CodeNode> startingClassNodes = startingNodes
                .flatMap(NodeStream::of)
                .filter(NodeModel::isLeaf)
                .map(CodeNode.class::cast);
        Stream<CodeNode> filteredStartingClassNodes = filter == null ? startingClassNodes : startingClassNodes.filter(filter::accepts);
        return filteredStartingClassNodes
	            .flatMap(node -> dependencyDirection.nodeDependencies.apply(node))
	            .map(map::getClassNodeId)
	            .collect(Collectors.toCollection(HashSet::new));
    }
}
