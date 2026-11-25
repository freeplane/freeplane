package org.freeplane.features.map.mindmapmode;

import java.util.Collection;

import org.freeplane.features.map.NodeModel;

public interface INodeOperationGuard {
	default String refusalOfDeletion(final Collection<NodeModel> nodes) {
		return null;
	}

	default String refusalOfTextChange(final NodeModel node, final String newText) {
		return null;
	}
}
