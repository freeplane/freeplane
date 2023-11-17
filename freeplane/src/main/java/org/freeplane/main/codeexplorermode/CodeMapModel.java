package org.freeplane.main.codeexplorermode;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.INodeDuplicator;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.MapStyleModel;

class CodeMapModel extends MapModel {
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


}
