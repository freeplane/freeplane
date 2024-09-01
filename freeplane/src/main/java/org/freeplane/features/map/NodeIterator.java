package org.freeplane.features.map;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class NodeIterator<T> implements Iterator<T>{
    public enum Algorithm {
        BOTTOM_UP, TOP_DOWN
    }

    private final T node;
    private final Function<T, Iterable<T>> childrenGetter;
    private Algorithm algorithm;
    private Iterator<T> children;
    private Iterator<T> subtree;
    boolean nodeReturned;

    public static <T> NodeIterator<T> of(T node, Function<T, Iterable<T>> children) {
        return new NodeIterator<T>(node, children, Algorithm.TOP_DOWN);
    }

    public static <T>NodeIterator<T> bottomUpOf(T node, Function<T, Iterable<T>> children) {
        return new NodeIterator<T>(node, children, Algorithm.BOTTOM_UP);
    }

    private NodeIterator(T node, Function<T, Iterable<T>> childrenGetter, Algorithm algorithm) {
        this.node = node;
        this.childrenGetter = childrenGetter;
        this.algorithm = algorithm;
        this.children = childrenGetter.apply(node).iterator();
        this.subtree = Collections.emptyIterator();
        this.nodeReturned = false;
    }

    @Override
    public boolean hasNext() {
        return ! nodeReturned || children.hasNext() || subtree.hasNext();
    }

    @Override
    public T next() {
        if(algorithm == Algorithm.TOP_DOWN && ! nodeReturned) {
            nodeReturned = true;
            return node;
        }
        if(subtree.hasNext())
            return subtree.next();
        if(children.hasNext()) {
            subtree = new NodeIterator<T>(children.next(), childrenGetter, algorithm);
            return subtree.next();
        }
        if(nodeReturned) {
            throw new NoSuchElementException();
        }
        nodeReturned = true;
        return node;
    }
}