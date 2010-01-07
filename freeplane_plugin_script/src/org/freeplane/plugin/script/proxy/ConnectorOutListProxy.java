/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.mindmapmode.MModeController;

class ConnectorOutListProxy extends AbstractCollection<Proxy.Connector> {
	private final MModeController modeController;
	private final NodeModel node;

	public ConnectorOutListProxy(final NodeModel node,
			final MModeController modeController) {
		this.node = node;
		this.modeController = modeController;
	}

	@Override
	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(NodeLinks.getLinks(node).iterator(),
				modeController);
	}

	@Override
	public int size() {
		return NodeLinks.getLinks(node).size();
	}
}