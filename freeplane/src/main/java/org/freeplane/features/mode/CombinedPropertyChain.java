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
package org.freeplane.features.mode;

import java.util.Iterator;
import java.util.TreeMap;

public class CombinedPropertyChain<V, T> {
	final private boolean reversed;
	public CombinedPropertyChain(boolean reversed) {
	    super();
	    this.reversed = reversed;
    }

	final private TreeMap<Integer, IPropertyHandler<V, T>> handlers = new TreeMap<Integer, IPropertyHandler<V, T>>();

	public IPropertyHandler<V, T> addGetter(final Integer key, final IPropertyHandler<V, T> getter) {
		return handlers.put(key(key), getter);
	}

	private int key(final int key) {
	    return reversed ? -key : key;
    }

	public V getProperty(final T node, V property) {
		final Iterator<IPropertyHandler<V, T>> iterator = handlers.values().iterator();
		while (iterator.hasNext()) {
			final IPropertyHandler<V, T> getter = iterator.next();
			property = getter.getProperty(node, property);
		}
		return property;
	}

	public IPropertyHandler<V, T> removeGetter(final Integer key) {
		return handlers.remove(key(key));
	}
}
