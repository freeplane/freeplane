/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.map.NodeModel;

class ConnectorOutListProxy extends AbstractCollection<Proxy.Connector> {
// 	private final MModeController modeController;
	private final NodeModel node;

	public ConnectorOutListProxy(final NodeModel node) {
		this.node = node;
	}

	@Override
	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(NodeLinks.getLinks(node).iterator());
	}

	@Override
	public int size() {
		return NodeLinks.getLinks(node).size();
	}
}
