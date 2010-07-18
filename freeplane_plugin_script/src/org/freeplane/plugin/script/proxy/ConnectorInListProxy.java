/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Set;

import org.freeplane.features.common.link.LinkModel;
import org.freeplane.features.common.link.MapLinks;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;

class ConnectorInListProxy extends AbstractCollection<Proxy.Connector> {
// 	private final MModeController modeController;
	private final NodeModel node;

	public ConnectorInListProxy(final NodeModel node) {
		this.node = node;
	}

	Set<LinkModel> getConnectorSet() {
		return MapLinks.getLinks(node.getMap()).get(node.getID());
	}

	@Override
	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(getConnectorSet().iterator());
	}

	@Override
	public int size() {
		return getConnectorSet().size();
	}
}
