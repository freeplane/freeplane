/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.freeplane.features.link.LinkModel;
import org.freeplane.features.link.MapLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

class ConnectorInListProxy extends AbstractCollection<Proxy.Connector> {
	private final NodeModel node;
	private final ScriptContext scriptContext;

	public ConnectorInListProxy(final NodeProxy nodeProxy) {
		this.node = nodeProxy.getDelegate();
		this.scriptContext = nodeProxy.getScriptContext();
	}

	List<LinkModel> getConnectorSet() {
		final Set<LinkModel> links = MapLinks.getLinks(node.getMap()).get(node.getID());
		return links == null ? Collections.<LinkModel> emptyList() : Collections
		    .unmodifiableList(new ArrayList<LinkModel>(links));
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
