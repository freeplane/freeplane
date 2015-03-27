/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.plugin.script.ScriptContext;

class ConnectorIterator implements Iterator<Proxy.Connector> {
	private final ScriptContext scriptContext;
	private final Iterator<NodeLinkModel> iterator;
	private ConnectorModel next;

	public ConnectorIterator(final Iterator<NodeLinkModel> iterator, final ScriptContext scriptContext) {
		this.scriptContext = scriptContext;
		this.iterator = iterator;
		next = getNextConnectorModel();
	}

	private ConnectorModel getNextConnectorModel() {
		while (iterator.hasNext()) {
			final NodeLinkModel linkModel = iterator.next();
			if (linkModel instanceof ConnectorModel) {
				return (ConnectorModel) linkModel;
			}
		}
		return null;
	}

	public boolean hasNext() {
		return next != null;
	}

	public Proxy.Connector next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		final ConnectorModel current = next;
		next = getNextConnectorModel();
		return new ConnectorProxy(current, scriptContext);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
