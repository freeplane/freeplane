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
package org.freeplane.map;

import java.util.Iterator;
import java.util.TreeMap;

public class ExclusivePropertyChain<V, T> {
	static public Integer DEFAULT = 100;
	static public Integer NODE = 10;
	final private TreeMap handlers = new TreeMap();

	public IPropertyGetter addGetter(final Integer key, final IPropertyGetter getter) {
		return (IPropertyGetter) handlers.put(key, getter);
	}

	public V getProperty(final T node) {
		final Iterator iterator = handlers.values().iterator();
		while (iterator.hasNext()) {
			final IPropertyGetter<V, T> getter = (IPropertyGetter) iterator.next();
			final V property = getter.getProperty(node, null);
			if (property != null) {
				return property;
			}
		}
		return null;
	}

	public IPropertyGetter removeGetter(final Integer key) {
		return (IPropertyGetter) handlers.remove(key);
	}
}
