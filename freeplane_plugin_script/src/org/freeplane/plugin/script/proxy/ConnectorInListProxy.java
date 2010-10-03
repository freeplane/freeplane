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
import org.freeplane.plugin.script.ScriptContext;

class ConnectorInListProxy extends AbstractCollection<Proxy.Connector> {
	private final NodeModel node;
	private final ScriptContext scriptContext;

	public ConnectorInListProxy(final NodeProxy nodeProxy) {
		this.node = nodeProxy.getDelegate();
		this.scriptContext = nodeProxy.getScriptContext();
	}

	Set<LinkModel> getConnectorSet() {
		return MapLinks.getLinks(node.getMap()).get(node.getID());
	}

	@Override
	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(getConnectorSet().iterator(), scriptContext);
	}

	@Override
	public int size() {
		return getConnectorSet().size();
	}
}
