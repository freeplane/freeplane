package org.freeplane.features.explorer;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class NodeAlias implements IExtension {
	final String value;

	private NodeAlias(String value) {
		super();
		this.value = value;
	}
	public static String getAlias(final NodeModel node) {
		final NodeAlias alias = node.getExtension(NodeAlias.class);
		return alias == null ? "" : alias.value;
	}
	public static void setAlias(NodeModel node, String alias) {
		if(alias == null || alias.isEmpty())
			removeAlias(node);
		else {
			NodeAlias a = new NodeAlias(alias);
			node.putExtension(a);
			NodeAliases.of(node.getMap()).add(a, node.createID());
		}
	}
	private static void removeAlias(NodeModel node) {
		node.removeExtension(NodeAlias.class);
	}
}
