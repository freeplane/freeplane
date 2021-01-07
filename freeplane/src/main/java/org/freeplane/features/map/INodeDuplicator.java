package org.freeplane.features.map;

public interface INodeDuplicator {
    NodeModel duplicate(final NodeModel source, boolean withChildren);
}
