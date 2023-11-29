package org.freeplane.plugin.codeexplorer.map;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.INodeDuplicator;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyJudge;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;

import com.tngtech.archunit.core.domain.Dependency;

class CodeMapModel extends MapModel {

    private DependencyJudge judge = DependencyJudge.of("");

    public CodeMapModel(INodeDuplicator nodeDuplicator) {
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

    DependencyVerdict judge(Dependency dependency, boolean goesUp) {
        return judge.judge(dependency, goesUp);
    }


}
