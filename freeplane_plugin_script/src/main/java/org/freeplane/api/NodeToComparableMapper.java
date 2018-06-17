package org.freeplane.api;

import org.freeplane.api.Proxy.NodeRO;

public interface NodeToComparableMapper {
	Comparable<Object> toComparable(NodeRO node);
}
