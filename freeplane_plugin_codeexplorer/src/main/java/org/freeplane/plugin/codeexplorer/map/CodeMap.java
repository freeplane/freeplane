package org.freeplane.plugin.codeexplorer.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.INodeDuplicator;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.plugin.codeexplorer.dependencies.CodeDependency;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;
import org.freeplane.plugin.codeexplorer.task.DependencyJudge;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

public class CodeMap extends MapModel {

    private DependencyJudge judge = new DependencyJudge();
    private SubprojectFinder subprojectFinder = SubprojectFinder.EMPTY;

    public CodeMap(INodeDuplicator nodeDuplicator) {
        super(nodeDuplicator);
        AttributeRegistry.getRegistry(this);

        setRoot(new EmptyNodeModel(this, "No locations selected"));
        getRootNode().setFolded(false);
    }

    @SuppressWarnings("serial")
    @Override
    protected Map<String, NodeModel> createNodeByIdMap() {
        return new ConcurrentHashMap<String, NodeModel>() {

            @Override
            public NodeModel put(String key, NodeModel value) {
                if(value == null)
                    return remove(key);
                else
                    return super.put(key, value);
            }

        };
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


    public String getClassNodeId(JavaClass javaClass) {
        int subprojectIndex = subprojectIndexOf(javaClass);
        return getClassNodeId(javaClass, subprojectIndex);
    }

    private String getClassNodeId(JavaClass javaClass, int subprojectIndex) {
        JavaClass nodeClass = CodeNode.findEnclosingNamedClass(javaClass);
        String nodeClassName = nodeClass.getName();
        String classNodeId = CodeNode.idWithSubprojectIndex(nodeClassName, subprojectIndex);
        return classNodeId;
    }

   public CodeNode getNodeByClass(JavaClass javaClass) {
        String existingNodeId = getExistingNodeId(javaClass);
        return existingNodeId == null ? null : (CodeNode) getNodeForID(existingNodeId);
    }

    String getExistingNodeId(JavaClass javaClass) {
        int subprojectIndex = subprojectIndexOf(javaClass);
        return getClassNodeId(javaClass, subprojectIndex);
    }

    public CodeDependency toCodeDependency(Dependency dependency) {
        NodeModel originNode = getNodeByClass(dependency.getOriginClass());
        NodeModel targetNode = getNodeByClass(dependency.getTargetClass());
        if(originNode == null || targetNode == null) {
            throw new IllegalStateException("Can not find nodes for dependency " + dependency);
        }
        NodeRelativePath nodeRelativePath = new NodeRelativePath(originNode, targetNode);
        boolean goesUp = nodeRelativePath.compareNodePositions() > 0;
        return new CodeDependency(dependency, goesUp, judge(dependency, goesUp));
    }

}
