/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Connector;

class ConnectorOutListProxy extends AbstractCollection<Proxy.Connector> {
	private final NodeModel node;
	private final ScriptContext scriptContext;

	public ConnectorOutListProxy(final NodeProxy nodeProxy) {
		this.node = nodeProxy.getDelegate();
		this.scriptContext = nodeProxy.getScriptContext();
	}

	@Override
	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(Collections.unmodifiableList(new ArrayList<NodeLinkModel>(NodeLinks.getLinks(node)))
		    .iterator(), scriptContext);
	}

	@Override
	public int size() {
		return NodeLinks.getLinks(node).size();
	}

	@Override
	public boolean removeIf(Predicate<? super Connector> filter) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Spliterator<Connector> spliterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Connector> stream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Connector> parallelStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forEach(Consumer<? super Connector> action) {
		// TODO Auto-generated method stub
	}
}
