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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeIterator;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.thirdparty.com.google.common.base.Supplier;

@SuppressWarnings("serial")
class ShowDependingNodesAction extends AFreeplaneAction {

    private DependencyDirection dependencyDirection;
    private NodeSelection nodeSelection;

    enum DependencyDirection {
        INCOMING(CodeNodeModel::getIncomingDependencies),
        OUTGOING(CodeNodeModel::getOutgoingDependencies),
        INCOMING_AND_OUTGOING(CodeNodeModel::getIncomingAndOutgoingDependencies);

        final Function<CodeNodeModel, Stream<Dependency>> nodeDependencies;

        private DependencyDirection(Function< CodeNodeModel, Stream<Dependency>> nodeDependencies) {
            this.nodeDependencies = nodeDependencies;
        }
    }

    enum NodeSelection {
        VISIBLE(ShowDependingNodesAction::visibleNodes),
        SELECTED(ShowDependingNodesAction::selectedNodes);

        private final Supplier<Stream<CodeNodeModel>> nodeSupplier;

        private NodeSelection(Supplier<Stream<CodeNodeModel>> nodeSupplier) {
            this.nodeSupplier = nodeSupplier;
        }

    }

    static Stream<CodeNodeModel> selectedNodes(){
        return Controller.getCurrentController().getSelection().getSelection().stream().map(CodeNodeModel.class::cast);
    }


    static Stream<CodeNodeModel> visibleNodes(){
        MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
        NodeIterator<NodeView> nodeViewIterator = NodeIterator.of(mapView.getRoot(), NodeView::getChildrenViews);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(nodeViewIterator, Spliterator.ORDERED), false)
                .filter(NodeView::isContentVisible)
                .map(NodeView::getNode)
                .map(CodeNodeModel.class::cast);
    }

    private static Stream<JavaClass> dependentClasses(Dependency dependency) {
        JavaClass origin = CodeNodeModel.findEnclosingNamedClass(dependency.getOriginClass());
        JavaClass target = CodeNodeModel.findEnclosingNamedClass(dependency.getTargetClass());
        return origin != target ? Stream.of(origin, target) : Stream.empty();
    }


    ShowDependingNodesAction(DependencyDirection dependencyDirection,
            NodeSelection nodeSelection) {
	    super("code.ShowDependingNodesAction." + dependencyDirection + "." + nodeSelection,
	            TextUtils.format("code.ShowDependingNodesAction.text",
	                    TextUtils.getRawText("code." + dependencyDirection),
	                    TextUtils.getRawText("code." + nodeSelection)),
	            null);
        this.dependencyDirection = dependencyDirection;
        this.nodeSelection = nodeSelection;
    }

	@Override
    public void actionPerformed(ActionEvent e) {
	    IMapSelection selection = Controller.getCurrentController().getSelection();
	    if(selection.getFilter().getCondition() == null)
	        return;
        Set<String> dependentNodeIDs = nodeSelection.nodeSupplier.get()
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
