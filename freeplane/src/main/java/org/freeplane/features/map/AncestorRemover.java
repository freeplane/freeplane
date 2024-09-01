/*
 * Created on 16 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.features.map;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.twelvemonkeys.util.LinkedSet;

public class AncestorRemover {
    public static Set<NodeModel> removeAncestors(Collection<NodeModel> nodes){
        Set<NodeModel> nonAncestralSelection = new LinkedSet<>();
        Set<NodeModel> nodesWithAncestors = new HashSet<>();
        for(NodeModel node:nodes) {
            if(nodesWithAncestors.contains(node))
                continue;
            nonAncestralSelection.add(node);
            for(NodeModel ancestor = node.getParentNode();
                    ancestor != null;
                    ancestor = ancestor.getParentNode()) {
                if(! nodesWithAncestors.add(ancestor))
                    break;
                nonAncestralSelection.remove(ancestor);
            }
        }
        return nonAncestralSelection;
    }
}
