/*
 * Created on 15 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.AncestorRemover;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

public class DependencySelection {
    private enum Visibility {VISIBLE, HIDDEN_BY_FILTER, HIDDEN_BY_FOLDING, UNKNOWN}

    private final IMapSelection selection;
    private CodeMap map;
    private Set<NodeModel> selectedNodeSet;
    private final boolean showsOutsideDependencies;

    public DependencySelection(IMapSelection selection) {
        this(selection, ResourceController.getResourceController().getBooleanProperty("code_showOutsideDependencies", true));
    }

    public DependencySelection(IMapSelection selection, boolean showsOutsideDependencies) {
        this.selection = selection;
        this.showsOutsideDependencies = showsOutsideDependencies;
    }

    public Stream<Dependency> getSelectedDependencies() {
        Set<NodeModel> nodes = AncestorRemover.removeAncestors(getSelectedNodeSet());
        Stream<Dependency> allDependencies = nodes.stream()
                .flatMap(node ->
                Stream.concat(
                        getOutgoingDependencies((CodeNode)node).stream(),
                        getIncomingDependencies((CodeNode)node).stream()))
                .distinct();
        return allDependencies;
    }

    List<JavaClass> getSelectedClasses() {
        List<JavaClass> allClasses;
        Set<NodeModel> nodes = AncestorRemover.removeAncestors(getSelectedNodeSet());
        allClasses = nodes.stream()
                .flatMap(node ->
                Stream.concat(
                        ((CodeNode)node).getOutgoingDependenciesWithKnownTargets().map(Dependency::getOriginClass),
                        ((CodeNode)node).getIncomingDependenciesWithKnownOrigins().map(Dependency::getTargetClass)))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        return allClasses;
    }

    public CodeNode getVisibleNode(JavaClass javaClass) {
        CodeMap map = getMap();
        for (CodeNode node = map.getNodeByClass(javaClass);
                node != null;
                node = node.getParentNode()) {
            switch(visibility(node)) {
            case VISIBLE:
                return node;
            case HIDDEN_BY_FILTER:
                return null;
            default:
                break;
            }
        }
        return null;
    }

    private Set<Dependency> getOutgoingDependencies(CodeNode node) {
         Stream<Dependency> dependencies = node.getOutgoingDependenciesWithKnownTargets();
         return dependenciesBetweenDifferentElements(dependencies);
     }
     private Set<Dependency> getIncomingDependencies(CodeNode node) {
         Stream<Dependency> dependencies = node.getIncomingDependenciesWithKnownOrigins();
         return dependenciesBetweenDifferentElements(dependencies);
     }

     private Set<Dependency> dependenciesBetweenDifferentElements(Stream<Dependency> dependencies) {
         Set<Dependency> filteredDependencies = dependencies
                 .filter(dependency -> connectsDifferentVisibleNodes(dependency))
                 .collect(Collectors.toSet());
         return filteredDependencies;
     }

     private NodeModel findSelectedAncestorOrSelf(NodeModel node) {
         while(node != null && ! selectionContains(node))
             node = node.getParentNode();
         return node;
     }

     NodeModel findVisibleAncestorOrSelf(NodeModel node) {
         while(node != null && ! selection.isVisible(node))
             node = node.getParentNode();
         return node;
     }

     private boolean selectionContains(NodeModel node) {
         return getSelectedNodeSet().contains(node);
     }

     private Set<NodeModel> getSelectedNodeSet() {
         if(selectedNodeSet == null)
             selectedNodeSet = selection.getSelection();
        return selectedNodeSet;
    }

    public CodeMap getMap() {
        if(map == null)
            map = (CodeMap) selection.getMap();
        return map;
    }

    private Visibility visibility(NodeModel node) {
        if(node == null)
            return Visibility.UNKNOWN;
        if(selection.isVisible(node))
            return Visibility.VISIBLE;
        if(! node.isVisible(selection.getFilter()))
            return Visibility.HIDDEN_BY_FILTER;
        return Visibility.HIDDEN_BY_FOLDING;
    }

    private boolean connectsDifferentVisibleNodes(Dependency dependency) {
        CodeNode visibleOrigin = getVisibleNode(dependency.getOriginClass());
        CodeNode visibleTarget = getVisibleNode(dependency.getTargetClass());
        return visibleOrigin != null && visibleTarget != null && visibleOrigin != visibleTarget
                && isConnectorSelected(visibleOrigin, visibleTarget);
    }

    public boolean isConnectorSelected(CodeNode origin, CodeNode target) {
        Set<NodeModel> selectedNodes = getSelectedNodeSet();
        boolean isOnlyOneNodeSelected = selectedNodes.size() == 1;
        NodeModel selectionRoot = selection.getSelectionRoot();
        if (origin == selectionRoot || target == selectionRoot)
            return false;
        if(isOnlyOneNodeSelected && selection.getSelected() == selectionRoot)
            return false;
        NodeModel selectedOriginAncestorOrOrigin = findSelectedAncestorOrSelf(origin);
        boolean isOriginSelected = selectedOriginAncestorOrOrigin != null;
        NodeModel selectedTargetAncestorOrTarget = findSelectedAncestorOrSelf(target);
        boolean isTargetSelected = selectedTargetAncestorOrTarget != null;
        if(! isOriginSelected && ! isTargetSelected)
            return false;
        boolean areOriginAndTargetSelected = isOriginSelected && isTargetSelected;
        if (isOnlyOneNodeSelected || showsOutsideDependencies)
            return ! areOriginAndTargetSelected;
        else if (areOriginAndTargetSelected)
            return selectedOriginAncestorOrOrigin != selectedTargetAncestorOrTarget;
        else return false;
    }
}