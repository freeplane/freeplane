package org.freeplane.api;

public interface NodeToComparableMapper {
	Comparable<Object> toComparable(NodeRO node);
}
