package org.freeplane.features.map;

import java.util.ArrayList;
import java.util.Collection;

public class NodeSubtrees { 
    public static ArrayList<NodeModel> getUniqueSubtreeRoots(final Collection<NodeModel> nodes) {
        final ArrayList<NodeModel> selectedNodes = new ArrayList<NodeModel>();
        ADD_NODES: for (final NodeModel nodeModel : nodes) {
            final NodeModel parentNode = nodeModel.getParentNode();
            if(parentNode != null){
                final int index = parentNode.getIndex(nodeModel);
                for(final NodeModel parentClone : parentNode.subtreeClones())
                    if(selectedNodes.contains(parentClone.getChildAt(index)))
                        continue ADD_NODES;
            }

            selectedNodes.add(nodeModel);
        }
        return selectedNodes;
    }
}