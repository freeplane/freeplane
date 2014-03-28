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
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.freeplane.features.link.MapLinks;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Connector;

class ConnectorInListProxy extends AbstractCollection<Proxy.Connector> {
	private final NodeModel node;
	private final ScriptContext scriptContext;

	public ConnectorInListProxy(final NodeProxy nodeProxy) {
		this.node = nodeProxy.getDelegate();
		this.scriptContext = nodeProxy.getScriptContext();
	}

	List<NodeLinkModel> getConnectorSet() {
		final MapLinks allLinks = MapLinks.getLinks(node.getMap());
        final Set<NodeLinkModel> links = allLinks == null ? null : allLinks.get(node.getID());
		return links == null ? Collections.<NodeLinkModel> emptyList() : Collections
		    .unmodifiableList(new ArrayList<NodeLinkModel>(links));
	}

	@Override
	public Iterator<Proxy.Connector> iterator() {
		return new ConnectorIterator(getConnectorSet().iterator(), scriptContext);
	}

	@Override
	public int size() {
		return getConnectorSet().size();
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
