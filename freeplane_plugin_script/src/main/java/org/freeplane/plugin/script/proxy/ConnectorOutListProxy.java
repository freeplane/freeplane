/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptExecution;

class ConnectorOutListProxy extends AbstractCollection<Proxy.Connector> {
	private final NodeModel node;
	private final ScriptExecution scriptExecution;

	public ConnectorOutListProxy(final NodeProxy nodeProxy) {
		this.node = nodeProxy.getDelegate();
		this.scriptExecution = nodeProxy.getScriptExecution();
	}

	@Override
	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(Collections.unmodifiableList(new ArrayList<NodeLinkModel>(NodeLinks.getLinks(node)))
		    .iterator(), scriptExecution);
	}

	@Override
	public int size() {
		return NodeLinks.getLinks(node).size();
	}
}
