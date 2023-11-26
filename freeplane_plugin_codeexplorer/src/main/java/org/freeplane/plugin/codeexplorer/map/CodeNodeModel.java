/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

public abstract class CodeNodeModel extends NodeModel {

    static String formatClassCount(long classCount) {
        return " (" + classCount + (classCount == 1 ? " class)" : " classes)");
    }

    static JavaClass findEnclosingNamedClass(JavaClass javaClass) {
        if (javaClass.isAnonymousClass())
            return findEnclosingNamedClass(javaClass.getEnclosingClass().get());
        else
            if(javaClass.isArray())
                return javaClass.getBaseComponentType();
            else
                return javaClass;
    }

    static JavaClass getTargetNodeClass(Dependency dependency) {
        return findEnclosingNamedClass(dependency.getTargetClass());
    }

    static boolean isNamed(JavaClass jc) {
        return ! jc.isAnonymousClass() && ! jc.isArray();
    }

    static boolean isTargetSourceKnown(Dependency dep) {
        return isClassSourceKnown(dep.getTargetClass());
    }

    static boolean isClassSourceKnown(JavaClass javaClass) {
        return javaClass.getSource().isPresent();
    }

    final private int subgroupIndex;

    CodeNodeModel(MapModel map, int subgroupIndex) {
        super(map);
        this.subgroupIndex = subgroupIndex;
    }



    @Override
    public CodeNodeModel getParentNode() {
        return (CodeNodeModel) super.getParentNode();
    }

    Set<CodeNodeModel> findCyclicDependencies() {return Collections.emptySet();}
    abstract Stream<Dependency> getOutgoingDependencies();
    abstract Stream<Dependency> getIncomingDependencies();
    abstract Set<JavaClass> getClassesInPackageTree();
    abstract String getUIIconName();

    Stream<Dependency> getIncomingAndOutgoingDependencies(){
        return Stream.concat(getIncomingDependencies(), getOutgoingDependencies());
    }

    public Stream<Dependency> getOutgoingDependenciesWithKnownTargets(){
        return getOutgoingDependencies().filter(CodeNodeModel::isTargetSourceKnown);
    }
    public Stream<Dependency> getIncomingDependenciesWithKnownTargets(){
        return getIncomingDependencies().filter(CodeNodeModel::isTargetSourceKnown);
    }
    Stream<Dependency> getIncomingAndOutgoingDependenciesWithKnownTargets(){
        return Stream.concat(getIncomingDependenciesWithKnownTargets(), getOutgoingDependenciesWithKnownTargets());
    }

    public int getSubgroupIndex() {
        return subgroupIndex;
    }

    @Override
    public List<NamedIcon> getIcons() {
        UIIcon uiIcon = IconStoreFactory.ICON_STORE.getUIIcon(getUIIconName());
        return Collections.singletonList(uiIcon);
    }

    abstract protected boolean initializeChildNodes();

    void loadSubtree() {
        if(initializeChildNodes())
            getChildrenInternal().forEach(node -> ((CodeNodeModel)node).loadSubtree());
    }
}
