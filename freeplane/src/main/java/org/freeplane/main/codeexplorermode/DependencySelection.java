/*
 * Created on 15 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;

class DependencySelection {
    private final IMapSelection selection;
    private MapModel map;
    private Set<NodeModel> selectedNodeSet;
    private int selectedSubtreesCount;

    DependencySelection(IMapSelection selection) {
        this.selection = selection;
    }

    List<Dependency> getSelectedDependencies() {
        List<Dependency> allDependencies;
        Set<NodeModel> nodes = selection.getSelection();
        selectedSubtreesCount = nodes.size();
        if(selectedSubtreesCount == 1) {
            CodeNodeModel selectedNode = (CodeNodeModel) selection.getSelected();
            Set<Dependency> outgoingDependencies = getOutgoingDependencies(selectedNode);
            Set<Dependency> incomingDependencies = getIncomingDependencies(selectedNode);
            allDependencies = new ArrayList<Dependency>(outgoingDependencies.size() + incomingDependencies.size());
            allDependencies.addAll(outgoingDependencies);
            allDependencies.addAll(incomingDependencies);
        }
        else {
            allDependencies = nodes.stream()
                    .map(node -> getOutgoingDependencies((CodeNodeModel)node))
                    .flatMap(Set::stream)
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
         return allDependencies;
     }

    String getVisibleNodeId(JavaClass javaClass) {
        JavaClass targetClass = CodeNodeModel.findEnclosingNamedClass(javaClass);
        String targetClassId = targetClass.getName();
        if(isVisible(targetClassId))
            return targetClassId;
        JavaPackage targetPackage = targetClass.getPackage();
        String targetPackageClassesId = targetPackage.getName() + ".package";
        if(isVisible(targetPackageClassesId))
            return targetPackageClassesId;
        HasName visiblePackage = getVisibleContainingPackage(targetPackage);
        return visiblePackage == null ? null : visiblePackage.getName();
    }

     private Set<Dependency> getOutgoingDependencies(CodeNodeModel node) {
         Stream<Dependency> dependencies = node.getOutgoingDependencies();
         return dependenciesBetweenDifferentElements(dependencies);
     }
     private Set<Dependency> getIncomingDependencies(CodeNodeModel node) {
         Stream<Dependency> dependencies = node.getIncomingDependencies();
         return dependenciesBetweenDifferentElements(dependencies);
     }

     private Set<Dependency> dependenciesBetweenDifferentElements(Stream<Dependency> dependencies) {
         Set<Dependency> filteredDependencies = dependencies
                         .filter(dependency -> connectsDifferentElements(dependency))
                         .collect(Collectors.toSet());
         return filteredDependencies;
     }
     private boolean connectsDifferentElements(Dependency dependency) {
         String visibleOriginId = getVisibleNodeId(dependency.getOriginClass());
         String visibleTargetId = getVisibleNodeId(dependency.getTargetClass());
         if (visibleOriginId == null  || visibleTargetId == null || visibleOriginId.equals(visibleTargetId))
             return false;
         if (selectedSubtreesCount == 1)
             return true;
         NodeModel visibleOrigin = findSelectedAncestorOrSelf(visibleOriginId);
         if(visibleOrigin ==  null)
             return false;
         NodeModel visibleTarget = findSelectedAncestorOrSelf(visibleTargetId);
         return visibleTarget != null && visibleTarget != visibleOrigin && ! visibleTarget.isDescendantOf(visibleOrigin) && ! visibleOrigin.isDescendantOf(visibleTarget);
     }

    private NodeModel findSelectedAncestorOrSelf(String id) {
        NodeModel node = getMap().getNodeForID(id);
         while(node != null && ! selectionContains(node))
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

    private MapModel getMap() {
        if(map == null)
            map = selection.getMap();
        return map;
    }


    private HasName getVisibleContainingPackage(JavaPackage targetPackage) {
         for(;;) {
             String targetPackageName = targetPackage.getName();
             NodeModel targetNode = getMap().getNodeForID(targetPackageName);
             if(targetNode != null) {
                 if(selection.isVisible(targetNode))
                     return targetPackage;
             }
             Optional<JavaPackage> parent = targetPackage.getParent();
             if(! parent.isPresent())
                 return null;
             targetPackage = parent.get();
         }
     }

     private boolean isVisible(String targetId) {
         boolean isVisible = false;
         NodeModel targetNode = getMap().getNodeForID(targetId);
         if(targetNode != null) {
             if(selection.isVisible(targetNode))
                 isVisible = true;
         }
         return isVisible;
     }

 }