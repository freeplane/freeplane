/*
 * Created on 5 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

class TreeInverseMap<V> implements TreeModelListener, TreeTagChangeListener<V> {
    private HashMap<V, Set<DefaultMutableTreeNode>> keysToNodesMap = new HashMap<>();
    private DefaultTreeModel treeModel;
    private final Function<DefaultMutableTreeNode, V> keyFunction;

    public TreeInverseMap(DefaultTreeModel treeModel, Function<DefaultMutableTreeNode, V> keyFunction) {
        this.treeModel = treeModel;
        this.keyFunction = keyFunction;
        this.treeModel.addTreeModelListener(this);
        buildInverseMap();
    }

    private void buildInverseMap() {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        traverseAndMapChildren(rootNode);
    }

    private void traverseAndMap(DefaultMutableTreeNode node) {
        addToMap(node);
        traverseAndMapChildren(node);
    }

    private void traverseAndRemove(DefaultMutableTreeNode node) {
        removeFromMap(node);
        traverseAndRemoveChildren(node);
    }

    private void traverseAndMapChildren(DefaultMutableTreeNode node) {
        final int addedNodeCount = node.getParent() == null ? node.getChildCount() - 1 : node.getChildCount();
        for (int i = 0; i < addedNodeCount; i++) {
            traverseAndMap((DefaultMutableTreeNode) node.getChildAt(i));
        }
    }

    private void traverseAndRemoveChildren(DefaultMutableTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            traverseAndRemove((DefaultMutableTreeNode) node.getChildAt(i));
        }
    }

    private void addToMap(DefaultMutableTreeNode node) {
        V key = keyFunction.apply(node);
        addToMap(key, node);
    }

    private boolean addToMap(V userObject, DefaultMutableTreeNode node) {
        return keysToNodesMap.computeIfAbsent(userObject, k -> new HashSet<>()).add(node);
    }

    private void removeFromMap(DefaultMutableTreeNode node) {
        @SuppressWarnings("unchecked")
        V userObject = (V) node.getUserObject();
        Set<DefaultMutableTreeNode> nodes = keysToNodesMap.get(userObject);
        if (nodes != null) {
            nodes.remove(node);
            if (nodes.isEmpty()) {
                keysToNodesMap.remove(userObject);
            }
        }
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        Object[] children = e.getChildren();
        for (Object child : children) {
            traverseAndMap((DefaultMutableTreeNode) child);
        }
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        Object[] children = e.getChildren();
        for (Object child : children) {
            traverseAndRemove((DefaultMutableTreeNode) child);
        }
    }
    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        // Optionally implement if changes to userObjects matter
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        keysToNodesMap.clear();
        buildInverseMap();
    }

    public Set<DefaultMutableTreeNode> getNodes(V userObject) {
        return keysToNodesMap.getOrDefault(userObject, Collections.emptySet());
    }

    @Override
    public void valueForPathChanged(TreePath path, V newValue) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        removeFromMap(node);
        addToMap(newValue, node);
    }
}