package org.freeplane.features.explorer;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.collection.FilterIterator;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class GlobalNodes implements IExtension, Iterable<NodeModel>{
    private static final GlobalNodes EMPTY = new GlobalNodes(Collections.<NodeModel, Void>emptyMap());
    private static boolean isContainedInTheMap(NodeModel key) {
        return key.getMap().getNodeForID(key.getID()) == key;
    }

    private final Map<NodeModel, Void> nodes;

    private GlobalNodes(Map<NodeModel, Void> nodes) {
        super();
        this.nodes = nodes;
    }



    private boolean contains(NodeModel key) {
        return nodes.containsKey(key) && isContainedInTheMap(key);
    }

    public static GlobalNodes writeableOf(MapModel map) {
        GlobalNodes globalNodes = map.getExtension(GlobalNodes.class);
        if(globalNodes == null) {
            globalNodes = new GlobalNodes(new WeakHashMap<NodeModel, Void>());
            map.addExtension(globalNodes);
        }
        return globalNodes;
    }

    static GlobalNodes readableOf(MapModel map) {
        GlobalNodes globalNodes = map.getExtension(GlobalNodes.class);
        return globalNodes == null ? EMPTY : globalNodes;
    }

    public void makeGlobal(NodeModel node, boolean isGlobal) {
        if(isGlobal)
            makeGlobal(node);
        else
            makeNotGlobal(node);
    }

    void makeGlobal(NodeModel node) {
        nodes.put(node, null);
    }

    private void makeNotGlobal(NodeModel node) {
        nodes.remove(node);
    }

    static boolean isGlobal(NodeModel node) {
        return readableOf(node.getMap()).contains(node);
    }
    
    @Override
    public Iterator<NodeModel> iterator() {
        return new FilterIterator<NodeModel>(nodes.keySet().iterator(), GlobalNodes::isContainedInTheMap);
    }
}
