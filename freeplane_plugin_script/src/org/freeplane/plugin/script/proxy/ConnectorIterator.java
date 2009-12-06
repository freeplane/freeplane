/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkModel;
import org.freeplane.features.mindmapmode.MModeController;

class ConnectorIterator implements Iterator<Proxy.Connector> {
	private final Iterator<LinkModel> iterator;
	private final MModeController modeController;
	private ConnectorModel next;

	public ConnectorIterator(final Iterator<LinkModel> iterator,
			final MModeController modeController) {
		this.iterator = iterator;
		this.modeController = modeController;
		this.next = getNextConnectorModel();
	}

	private ConnectorModel getNextConnectorModel() {
		while (iterator.hasNext()) {
			final LinkModel linkModel = iterator.next();
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
		return new ConnectorProxy(current, modeController);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}