/*
 * Created on 23 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

class NodeViewFolder {
    private final Set<NodeView> unfoldedNodeViews = new HashSet<NodeView>();

    void adjustFolding(Set<NodeView> selectedNodeViews) {
        Set<NodeView> selectedNodeViewsWithAncestors = withAncestors(selectedNodeViews);
        NodeView[] toFold = unfoldedNodeViews.stream()
                .filter(nodeView -> ! selectedNodeViewsWithAncestors.contains(nodeView) && nodeView.getNode().isFoldable())
                .toArray(NodeView[]::new);
        Stream.of(toFold).forEach(nodeView -> {
            nodeView.setFolded(true);
        });
        if(toFold.length == unfoldedNodeViews.size())
            unfoldedNodeViews.clear();
        else
            Stream.of(toFold).forEach(unfoldedNodeViews::remove);
        selectedNodeViews.stream()
        .filter(nodeView -> nodeView.isFolded())
        .forEach(nodeView -> {
            nodeView.setFolded(false);
            unfoldedNodeViews.add(nodeView);
        });
    }
    private HashSet<NodeView> withAncestors(Set<NodeView> nodeViews) {
        HashSet<NodeView> withAncestors = new HashSet<NodeView>();
        for (NodeView nodeView : nodeViews) {
            for (NodeView ancestor = nodeView;
                    ancestor != null && ! withAncestors.contains(ancestor);
                    ancestor = ancestor.getParentNodeView()) {
                withAncestors.add(ancestor);
            }
        }
        return withAncestors;
    }
}
