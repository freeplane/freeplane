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
package org.freeplane.core.modecontroller;

import java.util.Iterator;
import java.util.TreeMap;

@Deprecated
public class CombinedPropertyChain<V, T> {
	static public Integer DEFAULT = 10;
	static public Integer NODE = 100;
	final private TreeMap handlers = new TreeMap();

	public IPropertyHandler addGetter(final Integer key, final IPropertyHandler getter) {
		return (IPropertyHandler) handlers.put(key, getter);
	}

	public V getProperty(final T node) {
		final Iterator iterator = handlers.values().iterator();
		V property = null;
		while (iterator.hasNext()) {
			final IPropertyHandler<V, T> getter = (IPropertyHandler) iterator.next();
			property = getter.getProperty(node, property);
		}
		return property;
	}

	public IPropertyHandler removeGetter(final Integer key) {
		return (IPropertyHandler) handlers.remove(key);
	}
}
