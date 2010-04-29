/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Set;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.link.LinkModel;
import org.freeplane.features.common.link.MapLinks;
import org.freeplane.features.mindmapmode.MModeController;

class ConnectorInListProxy extends AbstractCollection<Proxy.Connector> {
	private final MModeController modeController;
	private final NodeModel node;

	public ConnectorInListProxy(final NodeModel node, final MModeController modeController) {
		this.node = node;
		this.modeController = modeController;
	}

	Set<LinkModel> getConnectorSet() {
		return MapLinks.getLinks(node.getMap()).get(node.getID());
	}

	@Override
	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(getConnectorSet().iterator(), modeController);
	}

	@Override
	public int size() {
		return getConnectorSet().size();
	}
}
