package org.freeplane.features.map;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NodeStream {
    public static Stream<NodeModel> bottomUpOf(NodeModel node) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(NodeIterator.bottomUpOf(node), Spliterator.ORDERED), false);
    }

    public static Stream<NodeModel> of(NodeModel node) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(NodeIterator.of(node), Spliterator.ORDERED), false);
    }
}