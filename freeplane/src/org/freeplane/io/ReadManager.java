/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.io;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.freeplane.map.tree.NodeModel;

public class ReadManager {
	final private Hashtable<String, Hashtable<String, IAttributeHandler>> attributeHandlers;
	final private ListHashTable<String, INodeContentHandler> nodeContentHandlers;
	final private ListHashTable<String, INodeCreator> nodeCreators;
	final private Collection<IReadCompletionListener> readCompletionListeners;
	final private ListHashTable<String, IXMLElementHandler> xmlHandlers;

	public ReadManager() {
		super();
		nodeCreators = new ListHashTable();
		attributeHandlers = new Hashtable<String, Hashtable<String, IAttributeHandler>>();
		nodeContentHandlers = new ListHashTable();
		xmlHandlers = new ListHashTable();
		readCompletionListeners = new LinkedList<IReadCompletionListener>();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#addAttributeLoader(java.lang.String,
	 * freeplane.persistence.AttributeLoader)
	 */
	public void addAttributeHandler(final String parentTag, final String attributeName,
	                                final IAttributeHandler a) {
		Hashtable<String, IAttributeHandler> tagHandlers = attributeHandlers.get(parentTag);
		if (tagHandlers == null) {
			tagHandlers = new Hashtable<String, IAttributeHandler>();
			attributeHandlers.put(parentTag, tagHandlers);
		}
		if (null != tagHandlers.put(attributeName, a)) {
			throw new RuntimeException("attribute handler " + parentTag + ", " + attributeName
			        + " already registered");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#addNodeContentLoader(java.lang.String,
	 * freeplane.persistence.NodeContentLoader)
	 */
	public void addNodeContentHandler(final String parentTag, final INodeContentHandler nc) {
		nodeContentHandlers.add(parentTag, nc);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#addNodeCreator(java.lang.String,
	 * freeplane.persistence.NodeCreator)
	 */
	public void addNodeCreator(final String parentTag, final INodeCreator n) {
		nodeCreators.add(parentTag, n);
	}

	public void addReadCompletionListener(final IReadCompletionListener listener) {
		readCompletionListeners.add(listener);
	}

	public void addXMLElementHandler(final String parentTag, final IXMLElementHandler x) {
		xmlHandlers.add(parentTag, x);
	}

	public Hashtable<String, Hashtable<String, IAttributeHandler>> getAttributeHandlers() {
		return attributeHandlers;
	}

	public ListHashTable getNodeContentHandlers() {
		return nodeContentHandlers;
	}

	public ListHashTable getNodeCreators() {
		return nodeCreators;
	}

	public ListHashTable getXmlHandlers() {
		return xmlHandlers;
	}

	public void readingCompleted(final NodeModel topNode, final HashMap<String, String> newIds) {
		final Iterator<IReadCompletionListener> iterator = readCompletionListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().readingCompleted(topNode, newIds);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#removeAttributeLoader(java.lang.String,
	 * freeplane.persistence.AttributeLoader)
	 */
	public void removeAttributeHandler(final String parentTag, final String attributeName,
	                                   final IAttributeHandler a) {
		final Hashtable<String, IAttributeHandler> hashtable = attributeHandlers.get(parentTag);
		hashtable.remove(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.persistence.Reader#removeNodeContentLoader(java.lang.String,
	 * freeplane.persistence.NodeContentLoader)
	 */
	public void removeNodeContentHandler(final String parentTag, final INodeContentHandler n) {
		nodeContentHandlers.remove(parentTag, n);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#removeNodeCreator(java.lang.String,
	 * freeplane.persistence.NodeCreator)
	 */
	public void removeNodeCreator(final String parentTag, final INodeCreator nc) {
		nodeCreators.remove(parentTag, nc);
	}

	public void removeReadCompletionListener(final IReadCompletionListener listener) {
		readCompletionListeners.remove(listener);
	}

	public void removeXMLElementHandler(final String parentTag, final IXMLElementHandler x) {
		xmlHandlers.remove(parentTag, x);
	}
}
