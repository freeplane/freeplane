package org.freeplane.features.map;

public interface INodeDuplicator {
    NodeModel duplicate(final NodeModel source, final MapModel targetMap,  boolean withChildren);
}
