/*
 * Created on 1 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.List;
import java.util.stream.Stream;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;

class ProjectRootNode extends CodeNode{
    static final String UI_ICON_NAME = "code_project";
    private final JavaPackage rootPackage;
    ProjectRootNode(CodeMap map, JavaPackage rootPackage, CodeExplorerConfiguration configuration) {
        super(map, 0);
        this.rootPackage = rootPackage;
        setID("projectRoot");
        setText(configuration.getProjectName());
    }

    @Override
    public List<NodeModel> getChildren() {
        initializeChildNodes();
        return super.getChildren();
    }

    @Override
    protected boolean initializeChildNodes() {
        List<NodeModel> children = super.getChildrenInternal();
        if (children.isEmpty()) {
            PackageNode node = new PackageNode(rootPackage, getMap(), "packages", subprojectIndex);
            children.add(node);
            node.setParent(this);
        }
        return false;
    }

    @Override
    HasName getCodeElement() {
        return () -> "root";
    }

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return Stream.empty();

    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return Stream.empty();
    }

    @Override
    String getUIIconName() {
        return UI_ICON_NAME;
    }

    public int subprojectIndexOf(JavaClass javaClass) {
        return javaClass.getSource().isPresent() ? 0 : -1;
    }

}
