/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;

abstract class CodeNodeModel extends NodeModel{

    CodeNodeModel(MapModel map) {
        super(map);
    }
    abstract Set<Dependency> getOutgoingDependencyCandidates(boolean includesDependenciesForChildPackages);
    abstract Set<Dependency> getIncomingDependencyCandidates(boolean includesDependenciesForChildPackages);

    boolean includesDependenciesForChildPackages(IMapSelection mapSelection) {
        return mapSelection.isFolded(this);
    }


    List<CodeConnectorModel> getOutgoingLinks(IMapSelection selection) {
        boolean includesDependenciesForChildPackages = includesDependenciesForChildPackages(selection);
        Set<Dependency> dependencies = getOutgoingDependencyCandidates(includesDependenciesForChildPackages);
        List<CodeConnectorModel> connectors = toConnectors(dependencies, selection);
        return connectors;
    }
    List<CodeConnectorModel> getIncomingLinks(IMapSelection selection) {
        boolean includesDependenciesForChildPackages = includesDependenciesForChildPackages(selection);
        Set<Dependency> dependencies = getIncomingDependencyCandidates(includesDependenciesForChildPackages);
        List<CodeConnectorModel> connectors = toConnectors(dependencies, selection);
        return connectors;
    }


    Set<Dependency> getOutgoingDependencies(IMapSelection selection) {
        boolean includesDependenciesForChildPackages = includesDependenciesForChildPackages(selection);
        Set<Dependency> dependencies = getOutgoingDependencyCandidates(includesDependenciesForChildPackages);
        return filter(dependencies, selection);
    }
    Set<Dependency> getIncomingDependencies(IMapSelection selection) {
        boolean includesDependenciesForChildPackages = includesDependenciesForChildPackages(selection);
        Set<Dependency> dependencies = getIncomingDependencyCandidates(includesDependenciesForChildPackages);
        return filter(dependencies, selection);
    }

    String getVisibleTargetName(IMapSelection mapSelection, Dependency dep) {
        JavaClass targetClass = findEnclosingNamedClass(dep.getTargetClass());
        String targetClassId = targetClass.getName();
        if(isVisible(mapSelection, targetClassId))
            return targetClassId;
        JavaPackage targetPackage = targetClass.getPackage();
        String targetPackageId = targetPackage.getName() + ".package";
        if(isVisible(mapSelection, targetPackageId))
            return targetPackageId;
        return getVisibleContainingPackageName(mapSelection, targetPackage);
    }

    private String getVisibleContainingPackageName(IMapSelection mapSelection, JavaPackage targetPackage) {
        for(;;) {
            String targetPackageName = targetPackage.getName();
            NodeModel targetNode = mapSelection.getMap().getNodeForID(targetPackageName);
            if(targetNode != null) {
                if(mapSelection.isVisible(targetNode))
                    return targetPackageName;
            }
            Optional<JavaPackage> parent = targetPackage.getParent();
            if(! parent.isPresent())
                return null;
            targetPackage = parent.get();
        }
    }

    private boolean isVisible(IMapSelection mapSelection, String targetId) {
        boolean isVisible = false;
        NodeModel targetNode = mapSelection.getMap().getNodeForID(targetId);
        if(targetNode != null) {
            if(mapSelection.isVisible(targetNode))
                isVisible = true;
        }
        return isVisible;
    }

    JavaClass findEnclosingNamedClass(JavaClass javaClass) {
        if (javaClass.isAnonymousClass())
            return findEnclosingNamedClass(javaClass.getEnclosingClass().get());
        else
            if(javaClass.isArray())
                return javaClass.getBaseComponentType();
            else
                return javaClass;
    }


    private Set<Dependency> filter(Set<Dependency> dependencies,
            IMapSelection mapSelection) {
        if(dependencies.isEmpty())
            return dependencies;

        Set<Dependency> filteredDependencies = new HashSet<>();
        for(Dependency dependency : dependencies) {
            String visibleTargetName = getVisibleTargetName(mapSelection, dependency);
            if(visibleTargetName != null && ! visibleTargetName.equals(getID()))
                filteredDependencies.add(dependency);
        }
        return filteredDependencies;
    }

    private List<CodeConnectorModel> toConnectors(Set<Dependency> dependencies,
            IMapSelection mapSelection) {
        if(dependencies.isEmpty())
            return Collections.emptyList();
        Map<String, Long> countedDependencies = dependencies.stream()
                .map(dep -> getVisibleTargetName(mapSelection, dep))
                .filter(name -> name != null && ! name.equals(getID()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<CodeConnectorModel> connectors = countedDependencies.entrySet().stream()
            .map(this::createConnector)
            .collect(Collectors.toList());
        return connectors;
    }

    private CodeConnectorModel createConnector(Entry<String, Long> e) {
        String targetId = e.getKey();
        NodeModel target = getMap().getNodeForID(targetId);
        NodeRelativePath nodeRelativePath = new NodeRelativePath(this, target);
        return new CodeConnectorModel(this, targetId, e.getValue().intValue(), nodeRelativePath.compareNodePositions() > 0);
    }

}
