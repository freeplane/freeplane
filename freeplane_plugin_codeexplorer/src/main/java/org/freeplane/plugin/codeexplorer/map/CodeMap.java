package org.freeplane.plugin.codeexplorer.map;

import java.util.stream.Stream;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.INodeDuplicator;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyJudge;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

public class CodeMap extends MapModel {

    private DependencyJudge judge = DependencyJudge.of("");
    private SubprojectFinder subprojectFinder = SubprojectFinder.EMPTY;

    public CodeMap(INodeDuplicator nodeDuplicator) {
        super(nodeDuplicator);
        AttributeRegistry.getRegistry(this);

        setRoot(new EmptyNodeModel(this, "No locations selected"));
        getRootNode().setFolded(false);
    }
    @Override
    public String getTitle() {
        return "Code: " + getRootNode().toString();
    }

    public void setJudge(DependencyJudge judge) {
        this.judge = judge;
    }

    @Override
    public void setRoot(NodeModel newRoot) {
        NodeModel oldRoot = getRootNode();
        if(oldRoot != null) {
            MapStyleModel mapStyles = oldRoot.getExtension(MapStyleModel.class);
            if(mapStyles != null)
                newRoot.addExtension(mapStyles);
        }
        super.setRoot(newRoot);
    }


    public void setSubprojectFinder(SubprojectFinder subprojectFinder) {
        this.subprojectFinder = subprojectFinder;
    }

    DependencyVerdict judge(Dependency dependency, boolean goesUp) {
        return judge.judge(dependency, goesUp);
    }

    public int subprojectIndexOf(JavaClass javaClass) {
        return subprojectFinder.subprojectIndexOf(javaClass);
    }
    public Stream<JavaClass> allClasses() {
        return subprojectFinder.allClasses();
    }
}
