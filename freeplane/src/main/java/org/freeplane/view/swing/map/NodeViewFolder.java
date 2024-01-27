/*
 * Created on 23 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;

class NodeViewFolder {
    private final Map<NodeView, Void> unfoldedNodeViews = new WeakHashMap<>();

    void foldingWasSet(NodeView view) {
        if(unfoldedNodeViews.containsKey(view))
            unfoldedNodeViews.remove(view);
    }


    void adjustFolding(Set<NodeView> selectedNodeViews) {
        Set<NodeView> selectedNodeViewsWithAncestors = withAncestors(selectedNodeViews);
        NodeView[] toFold = unfoldedNodeViews.keySet().stream()
                .filter(nodeView -> ! selectedNodeViewsWithAncestors.contains(nodeView)
                && SwingUtilities.isDescendingFrom(nodeView, nodeView.getMap()))
                .toArray(NodeView[]::new);
        Stream.of(toFold)
        .filter(nodeView -> nodeView.getNode().isFoldable())
        .forEach(nodeView -> nodeView.setFolded(true));

        selectedNodeViews.stream()
        .forEach(nodeView -> {
            boolean hasUnfoldView = false;
            if (nodeView.isFolded()) {
                nodeView.setFolded(false);
                unfoldedNodeViews.put(nodeView, null);
                hasUnfoldView = true;
            }

            for( NodeView descendant = nodeView;;) {
                LinkedList<NodeView> childrenViews = descendant.getChildrenViews();
                if (childrenViews.size() != 1)
                    break;
                descendant = childrenViews.get(0);
                if(descendant.isFolded()) {
                    descendant.setFolded(false);
                    if(! hasUnfoldView) {
                        unfoldedNodeViews.put(descendant, null);
                        hasUnfoldView = true;
                    }
                }
            }
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
