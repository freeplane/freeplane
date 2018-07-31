package org.freeplane.features.explorer.mindmapmode;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

class NodeAlias implements IExtension {
	public final String value;

	public NodeAlias(String value) {
		super();
		this.value = value;
	}
	static String getAlias(final NodeModel node) {
		final NodeAlias alias = node.getExtension(NodeAlias.class);
		return alias == null ? "" : alias.value;
	}
	static void setAlias(NodeModel node, String alias) {
		if(alias == null || alias.isEmpty())
			removeAlias(node);
		else
			node.putExtension(new NodeAlias(alias));
	}
	static void removeAlias(NodeModel node) {
		node.removeExtension(NodeAlias.class);
	}

}
