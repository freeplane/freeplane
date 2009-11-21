/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.plugin.script.proxy.Proxy.Connector;

class ConnectorIterator implements Iterator<Proxy.Connector> {
	private Iterator<LinkModel> iterator;
	private MModeController modeController;
	private ConnectorModel next = getNextConnectorModel();

	public ConnectorIterator(Iterator<LinkModel> iterator, MModeController modeController) {
		this.iterator = iterator;
		this.modeController = modeController;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	private ConnectorModel getNextConnectorModel() {
		while(iterator.hasNext()){
			LinkModel linkModel = iterator.next();
			if(linkModel instanceof ConnectorModel){
				return (ConnectorModel) linkModel;
			}
		}
		return null;
	}

	public Proxy.Connector next() {
		if(! hasNext()){
			throw new NoSuchElementException();
		}
		ConnectorModel current = next;
		next = getNextConnectorModel();
		return new ConnectorProxy(current, modeController);
	}

	public boolean hasNext() {
		return next != null;
	}
}