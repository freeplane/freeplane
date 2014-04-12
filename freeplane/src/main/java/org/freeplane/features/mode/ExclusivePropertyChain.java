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
import java.util.Map;
import java.util.TreeMap;

public class ExclusivePropertyChain<V, T> {
	final private Map<Integer, IPropertyHandler<V, T>> map = new TreeMap<Integer, IPropertyHandler<V, T>>();

	public IPropertyHandler<V, T> addGetter(final Integer key, final IPropertyHandler<V, T> getter) {
		return map.put(key, getter);
	}

	public V getProperty(final T node) {
		final Iterator<IPropertyHandler<V, T>> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			IPropertyHandler<V, T> handler = iterator.next();
			final V property = handler.getProperty(node, null);
			if (property != null) {
				return property;
			}
		}
		return null;
	}

	public IPropertyHandler<V, T> removeGetter(final Integer key) {
		return map.remove(key);
	}
}
