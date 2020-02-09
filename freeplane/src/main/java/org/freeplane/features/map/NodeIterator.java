package org.freeplane.features.map;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class NodeIterator implements Iterator<NodeModel>{
    public enum Algorithm {
        BOTTOM_UP, TOP_DOWN
    }

    private NodeModel node;
    private Algorithm algorithm;
    private Iterator<NodeModel> children;
    private Iterator<NodeModel> subtree;
    boolean nodeReturned;

    public static NodeIterator of(NodeModel node) {
        return new NodeIterator(node, Algorithm.TOP_DOWN);
    }
    
    public static NodeIterator bottomUpOf(NodeModel node) {
        return new NodeIterator(node, Algorithm.BOTTOM_UP);
    }

    private NodeIterator(NodeModel node, Algorithm algorithm) {
        this.node = node;
        this.algorithm = algorithm;
        children = node.getChildren().iterator();
        subtree = Collections.emptyIterator();
        nodeReturned = false;
    }

    @Override
    public boolean hasNext() {
        return ! nodeReturned || children.hasNext() || subtree.hasNext();
    }

    @Override
    public NodeModel next() {
        if(algorithm == Algorithm.TOP_DOWN && ! nodeReturned) {
            nodeReturned = true;
            return node;
        }
        if(subtree.hasNext())
            return subtree.next();
        if(children.hasNext()) {
            subtree = new NodeIterator(children.next(), algorithm);
            return subtree.next();
        }
        if(nodeReturned) {
            throw new NoSuchElementException();
        }
        nodeReturned = true;
        return node;
    }
}