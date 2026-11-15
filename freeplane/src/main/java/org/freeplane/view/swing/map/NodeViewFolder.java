/*
 * Created on 23 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

public class NodeViewFolder {
    private final Set<NodeView> unfoldedNodeViews = Collections.newSetFromMap(new WeakHashMap<>());
    private final boolean unfoldsSingleChildren;



    public NodeViewFolder(boolean unfoldsSingleChildren) {
		super();
		this.unfoldsSingleChildren = unfoldsSingleChildren;
	}

	public void foldingWasSet(NodeView view) {
        if(unfoldedNodeViews.contains(view))
            unfoldedNodeViews.remove(view);
    }

    public void reset() {
    	unfoldedNodeViews.clear();
    }


    <T> T withPreservedState(Supplier<T> operation) {
        Set<NodeView> savedUnfoldedNodeViews = new HashSet<>(unfoldedNodeViews);
        try {
            return operation.get();
        } finally {
            unfoldedNodeViews.clear();
            unfoldedNodeViews.addAll(savedUnfoldedNodeViews);
        }
    }

    public void adjustFolding(Set<NodeView> unfoldNodeViews) {
        Set<NodeView> unfoldNodeViewsWithAncestors = withAncestors(unfoldNodeViews);
        NodeView[] toFold = unfoldedNodeViews.stream()
                .filter(nodeView -> ! unfoldNodeViewsWithAncestors.contains(nodeView)
                && SwingUtilities.isDescendingFrom(nodeView, nodeView.getMap()))
                .toArray(NodeView[]::new);
        Stream.of(toFold)
        .filter(nodeView -> nodeView.getNode().isFoldable())
        .forEach(nodeView -> nodeView.setFolded(true));

        if(unfoldNodeViews.isEmpty())
        	unfoldedNodeViews.clear();
        else
        	unfoldNodeViews.removeAll(Arrays.asList(toFold));

        unfoldNodeViews.stream()
        .forEach(nodeView -> {
            boolean hasUnfoldView = false;
            if (nodeView.isFolded()) {
                nodeView.setFolded(false);
                unfoldedNodeViews.add(nodeView);
                hasUnfoldView = true;
            }

            if(unfoldsSingleChildren) {
				for( NodeView descendant = nodeView;;) {
				    LinkedList<NodeView> childrenViews = descendant.getChildrenViews();
				    if (childrenViews.size() != 1)
				        break;
				    descendant = childrenViews.get(0);
				    if(descendant.isFolded()) {
				        descendant.setFolded(false);
				        if(! hasUnfoldView) {
				            unfoldedNodeViews.add(descendant);
				            hasUnfoldView = true;
				        }
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
