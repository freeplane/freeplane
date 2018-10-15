package org.freeplane.api;

import java.util.Map;

public interface DependencyLookup {
	Map <? extends NodeRO, Dependencies> ofNode();
	Map <? extends NodeRO, Dependencies> ofAttribute(int attributeIndex);
	Map <? extends NodeRO, Dependencies> ofAttribute(String attributeName);
}
