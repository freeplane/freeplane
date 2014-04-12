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
package org.freeplane.core.io;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.freeplane.features.map.NodeModel;

public class ReadManager {
	final private Hashtable<String, Hashtable<String, IAttributeHandler>> attributeHandlers;
	final private ListHashTable<String, IElementHandler> elementHandlers;
	final private Collection<IReadCompletionListener> readCompletionListeners;

	public ReadManager() {
		super();
		elementHandlers = new ListHashTable<String, IElementHandler>();
		attributeHandlers = new Hashtable<String, Hashtable<String, IAttributeHandler>>();
		readCompletionListeners = new LinkedList<IReadCompletionListener>();
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#addAttributeLoader(java.lang.String,
	 * freeplane.persistence.AttributeLoader)
	 */
	public void addAttributeHandler(final String parentTag, final String attributeName, final IAttributeHandler a) {
		Hashtable<String, IAttributeHandler> tagHandlers = attributeHandlers.get(parentTag);
		if (tagHandlers == null) {
			tagHandlers = new Hashtable<String, IAttributeHandler>();
			attributeHandlers.put(parentTag, tagHandlers);
		}
		if (null != tagHandlers.put(attributeName, a)) {
			throw new RuntimeException("attribute handler " + parentTag + ", " + attributeName + " already registered");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#addNodeCreator(java.lang.String,
	 * freeplane.persistence.NodeCreator)
	 */
	public void addElementHandler(final String parentTag, final IElementHandler handler) {
		elementHandlers.add(parentTag, handler);
	}

	public void addReadCompletionListener(final IReadCompletionListener listener) {
		readCompletionListeners.add(listener);
	}

	public Hashtable<String, Hashtable<String, IAttributeHandler>> getAttributeHandlers() {
		return attributeHandlers;
	}

	public ListHashTable<String, IElementHandler> getElementHandlers() {
		return elementHandlers;
	}

	public void readingCompleted(final NodeModel topNode, final Map<String, String> newIds) {
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
	public void removeAttributeHandler(final String parentTag, final String attributeName, final IAttributeHandler a) {
		final Hashtable<String, IAttributeHandler> hashtable = attributeHandlers.get(parentTag);
		hashtable.remove(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.persistence.Reader#removeNodeCreator(java.lang.String,
	 * freeplane.persistence.NodeCreator)
	 */
	public void removeElementHandler(final String parentTag, final IElementHandler handler) {
		final boolean removed = elementHandlers.remove(parentTag, handler);
		assert removed;
	}

	public void removeReadCompletionListener(final IReadCompletionListener listener) {
		final boolean removed = readCompletionListeners.remove(listener);
		assert removed;
	}
}
