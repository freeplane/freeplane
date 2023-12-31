/*
 * Created on 23 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;

class NodeViewFolder {
    private final Map<NodeView, Void> unfoldedNodeViews = new WeakHashMap<>();

    void adjustFolding(Set<NodeView> selectedNodeViews) {
        Set<NodeView> selectedNodeViewsWithAncestors = withAncestors(selectedNodeViews);
        NodeView[] toFold = unfoldedNodeViews.keySet().stream()
                .filter(nodeView ->
                    ! selectedNodeViewsWithAncestors.contains(nodeView)
                    && nodeView.getNode().isFoldable()
                    && SwingUtilities.isDescendingFrom(nodeView, nodeView.getMap()))
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
            unfoldedNodeViews.put(nodeView, null);
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
