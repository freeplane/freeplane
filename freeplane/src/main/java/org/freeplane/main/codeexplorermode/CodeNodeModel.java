/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.freeplane.core.extension.Configurable;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;

abstract class CodeNodeModel extends NodeModel{

    CodeNodeModel(MapModel map) {
        super(map);
    }

    boolean includesDependenciesForChildPackages(MapView mapView) {
        return mapView.getNodeView(this).isFolded();
    }


    abstract Collection<? extends CodeConnectorModel> getOutgoingLinks(Configurable component);
    abstract Collection<? extends CodeConnectorModel> getIncomingLinks(Configurable component);

    String getVisibleTargetName(MapView mapView, Dependency dep) {
        JavaClass targetClass = findEnclosingNamedClass(dep.getTargetClass());
        String targetClassId = targetClass.getName();
        if(isVisible(mapView, targetClassId))
            return targetClassId;
        JavaPackage targetPackage = targetClass.getPackage();
        String targetPackageId = targetPackage.getName() + ".package";
        if(isVisible(mapView, targetPackageId))
            return targetPackageId;
        return getVisibleContainingPackageName(mapView, targetPackage);
    }

    private String getVisibleContainingPackageName(MapView mapView, JavaPackage targetPackage) {
        for(;;) {
            String targetPackageName = targetPackage.getName();
            NodeModel targetNode = mapView.getMap().getNodeForID(targetPackageName);
            if(targetNode != null) {
                NodeView targetView = mapView.getNodeView(targetNode);
                if(targetView != null)
                    return targetPackageName;
            }
            Optional<JavaPackage> parent = targetPackage.getParent();
            if(! parent.isPresent())
                return null;
            targetPackage = parent.get();
        }
    }

    private boolean isVisible(MapView mapView, String targetId) {
        boolean isVisible = false;
        NodeModel targetNode = mapView.getMap().getNodeForID(targetId);
        if(targetNode != null) {
            NodeView targetView = mapView.getNodeView(targetNode);
            if(targetView != null)
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


    List<CodeConnectorModel> toConnectors(Set<Dependency> packageDependencies,
            MapView mapView) {
        Map<String, Long> dependencies = packageDependencies.stream()
                .map(dep -> getVisibleTargetName(mapView, dep))
                .filter(name -> name != null && ! name.equals(getID()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<CodeConnectorModel> connectors = dependencies.entrySet().stream()
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
