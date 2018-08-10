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
		else {
			NodeAlias a = new NodeAlias(alias);
			node.putExtension(a);
			NodeAliases.of(node.getMap()).add(a);
		}
	}
	static void removeAlias(NodeModel node) {
		node.removeExtension(NodeAlias.class);
	}
	@Override
	public int hashCode() {
		int result= value.hashCode();
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeAlias other = (NodeAlias) obj;
		if (!value.equals(other.value))
			return false;
		return true;
	}

}
