package org.freeplane.features.explorer.mindmapmode;

import org.freeplane.features.map.NodeModel;

public interface AccessedNodes {
	AccessedNodes IGNORE = new AccessedNodes() {
		@Override
		public void accessNode(NodeModel accessedNode) {}

		@Override
		public void accessBranch(NodeModel accessedNode) {}

		@Override
		public void accessAll() {}
	};

	void accessNode(final NodeModel accessedNode);
	void accessBranch(final NodeModel accessedNode);
	void accessAll();
}
