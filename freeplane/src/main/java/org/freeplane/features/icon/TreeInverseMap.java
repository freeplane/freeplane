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

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

class TreeInverseMap<V> implements TreeModelListener, TreeTagChangeListener<V> {
    private HashMap<V, Set<DefaultMutableTreeNode>> userObjectToNodesMap = new HashMap<>();
    private DefaultTreeModel treeModel;

    public TreeInverseMap(DefaultTreeModel treeModel) {
        this.treeModel = treeModel;
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
        @SuppressWarnings("unchecked")
        V userObject = (V) node.getUserObject();
        addToMap(userObject, node);
    }

    private boolean addToMap(V userObject, DefaultMutableTreeNode node) {
        return userObjectToNodesMap.computeIfAbsent(userObject, k -> new HashSet<>()).add(node);
    }

    private void removeFromMap(DefaultMutableTreeNode node) {
        @SuppressWarnings("unchecked")
        V userObject = (V) node.getUserObject();
        Set<DefaultMutableTreeNode> nodes = userObjectToNodesMap.get(userObject);
        if (nodes != null) {
            nodes.remove(node);
            if (nodes.isEmpty()) {
                userObjectToNodesMap.remove(userObject);
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
        userObjectToNodesMap.clear();
        buildInverseMap();
    }

    public Set<DefaultMutableTreeNode> getNodes(V userObject) {
        return userObjectToNodesMap.getOrDefault(userObject, Collections.emptySet());
    }

    @Override
    public void valueForPathChanged(TreePath path, V newValue) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        removeFromMap(node);
        addToMap(newValue, node);
    }
}