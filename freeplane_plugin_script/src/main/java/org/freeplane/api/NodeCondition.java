package org.freeplane.api;

import org.freeplane.api.Proxy.NodeRO;

public interface NodeCondition {
	boolean check(NodeRO node);
}
