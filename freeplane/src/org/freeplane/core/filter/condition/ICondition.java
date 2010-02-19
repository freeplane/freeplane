package org.freeplane.core.filter.condition;

import org.freeplane.core.model.NodeModel;

public interface ICondition {
	boolean checkNode(NodeModel node);
}
