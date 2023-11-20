/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

abstract class CodeNodeModel extends NodeModel{

    static boolean isNamed(JavaClass jc) {
        return ! jc.isAnonymousClass() && ! jc.isArray();
    }

    static boolean isTargetSourceKnown(Dependency dep) {
        return dep.getTargetClass().getSource().isPresent();
    }

    CodeNodeModel(MapModel map) {
        super(map);
    }


    @Override
    public List<NamedIcon> getIcons() {
        UIIcon uiIcon = IconStoreFactory.ICON_STORE.getUIIcon(getUIIconName());
        return Collections.singletonList(uiIcon);
    }

    abstract String getUIIconName();

    abstract Stream<Dependency> getOutgoingDependencies();
    abstract Stream<Dependency> getIncomingDependencies();

    Stream<Dependency> getIncomingAndOutgoingDependencies(){
        return Stream.concat(getIncomingDependencies(), getOutgoingDependencies());
    }

    Stream<Dependency> getOutgoingDependenciesWithKnownTargets(){
        return getOutgoingDependencies().filter(CodeNodeModel::isTargetSourceKnown);
    }
    Stream<Dependency> getIncomingDependenciesWithKnownTargets(){
        return getIncomingDependencies().filter(CodeNodeModel::isTargetSourceKnown);
    }
    Stream<Dependency> getIncomingAndOutgoingDependenciesWithKnownTargets(){
        return Stream.concat(getIncomingDependenciesWithKnownTargets(), getOutgoingDependenciesWithKnownTargets());
    }

    String formatClassCount(long classCount) {
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

}
